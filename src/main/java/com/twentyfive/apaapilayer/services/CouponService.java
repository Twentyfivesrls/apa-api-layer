package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.*;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.InvalidCouponException;
import com.twentyfive.apaapilayer.exceptions.InvalidCustomerIdException;
import com.twentyfive.apaapilayer.mappers.CouponMapperService;
import com.twentyfive.apaapilayer.mappers.SummaryOrderMapperService;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.JwtUtilities;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Coupon;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.NumberRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapperService couponMapperService;
    private final SummaryOrderMapperService summaryOrderMapperService;
    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    private final CategoryService categoryService;

    private final CustomerRepository customerRepository;

    private final ProductFixedRepository productFixedRepository;
    private final ProductKgRepository productKgRepository;
    private final TrayRepository trayRepository;

    private final EmailService emailService;

    public Page<CouponDTO> getAll(int page, int size, String sortColumn, String sortDirection,boolean expired) {
        Sort sort;
        if (sortColumn == null || sortColumn.isBlank() || sortDirection == null || sortDirection.isBlank()) {
            sort = Sort.by(Sort.Direction.ASC, "name");
        } else {
            sort = Sort.by(Sort.Direction.fromString(sortDirection),sortColumn);
        }
        Pageable pageable= PageRequest.of(page,size,sort);
        List<Coupon> coupons = couponRepository.findAllByExpiredAndSoftDeletedFalse(expired);
        List<CouponDTO> dtos = couponMapperService.mapCouponsToDTO(coupons);
        return PageUtilities.convertListToPageWithSorting(dtos, pageable);
    }

    public CouponDetailsDTO getById(String id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new InvalidCouponException());
        List<CategoryMinimalDTO> categories = new ArrayList<>();
        if(coupon.getSpecificCategoriesId() != null) {
            categories = categoryService.getAllMinimalByListId(coupon.getSpecificCategoriesId());
        }
        return couponMapperService.mapCouponToDetailsDTO(coupon,categories);
    }
    public Coupon getByCode(String code) {
        return couponRepository.findByCodeAndSoftDeletedFalse(code)
                .orElse(null);
    }
    public Coupon save(Coupon coupon) {
        if(couponRepository.existsByCodeAndSoftDeletedFalseAndIdNot(coupon.getCode(), coupon.getId())) {
            throw new InvalidCouponException();
        }
        coupon.setExpired(coupon.checkExpiredCoupon());
        return couponRepository.save(coupon);
    }


    public Boolean deleteById(String id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new InvalidCouponException());
        coupon.setSoftDeleted(true);
        couponRepository.save(coupon);
        return true;
    }

    public Boolean changeStatus(String id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new InvalidCouponException());
        coupon.setActive(!coupon.isActive());
        couponRepository.save(coupon);
        return true;
    }

    public SummaryOrderDTO checkCoupon(CouponValidationReq couponValidationReq) throws IOException {
        // Recupera il coupon
        Coupon coupon = couponRepository.findByCodeAndSoftDeletedFalse(couponValidationReq.getCouponCode())
                .orElseThrow(InvalidCouponException::new);

        // Recupera il customer
        String idKeycloak = JwtUtilities.getIdKeycloak();
        CustomerAPA customer = customerRepository.findByIdKeycloak(idKeycloak)
                .orElseThrow(InvalidCustomerIdException::new);

        // Recupera gli item specificati nel carrello
        List<ItemInPurchase> purchases = customer.getCart().getItemsAtPositions(couponValidationReq.getPositions());

        // Controlli sul coupon
        CouponValidation validationStatus = validateCoupon(coupon, customer, purchases);

        double discountApplied = 0; // Variabile per tracciare lo sconto totale applicato

        // Applica il coupon al carrello
        if (validationStatus == CouponValidation.VALID) {
            discountApplied = applyCouponToPurchasesAndCalculateDiscount(coupon, purchases, false);
        }
        // Calcola il prezzo totale dopo gli sconti
        double finalTotalPrice = purchases.stream()
                .mapToDouble(ItemInPurchase::getTotalPrice)
                .sum();
        if(coupon.getSpecificCategoriesId()!=null && coupon.getSpecificCategoriesId().size() == 0){
            finalTotalPrice-=discountApplied;
        }


        // Genera la lista dei dettagli degli articoli
        List<SummarySingleItemDTO> summaryItems = generateSummaryItems(purchases);

        // Genera e ritorna il SummaryOrderDTO
        return summaryOrderMapperService.mapBuyInfosToSummaryOrder(summaryItems, validationStatus, discountApplied, finalTotalPrice);
    }


    public CouponValidation validateCoupon(Coupon coupon, CustomerAPA customer, List<ItemInPurchase> purchases) {
        if (coupon == null || !coupon.isActive()){
            return CouponValidation.NOT_VALID;
        }
        if (coupon.checkExpiredCoupon()) {
            return CouponValidation.EXPIRED;
        }

        if (coupon.getMaxTotalUsage() != null && coupon.getUsageCount() >= coupon.getMaxTotalUsage()) {
            return CouponValidation.MAX_USAGE;
        }

        if (!isUsageWithinLimit(coupon, customer.getId())) {
            return CouponValidation.LIMIT_USAGE;
        }
        if (coupon.getSpecificCategoriesId() !=null && coupon.getSpecificCategoriesId().size()>0 ){
            if (!isItemEligibleForCoupon(coupon, purchases) || !isPriceInRange(purchases,coupon.getPriceRange())) {
                return CouponValidation.NOT_ELIGIBLE;
            }
        }

        if (coupon.getSpecificCategoriesId() == null || coupon.getSpecificCategoriesId().size() == 0) {
            double totalPrice = purchases.stream()
                    .mapToDouble(ItemInPurchase::getTotalPrice) // Estrae il prezzo totale di ogni item
                    .sum(); // Somma tutti i prezzi
            if (!isWithinRange(totalPrice,coupon.getPriceRange())){
                return CouponValidation.NOT_ELIGIBLE;
            }
        }

        return CouponValidation.VALID;
    }

    private boolean isUsageWithinLimit(Coupon coupon, String customerId) {
        // Recupera il numero di utilizzi specifico del cliente
        return couponUsageRepository.findByIdCouponAndIdCustomer(coupon.getId(), customerId)
                .map(couponUsage -> couponUsage.getUsageCount() < coupon.getMaxUsagePerCustomer())
                .orElse(true); // Se non esiste un record, significa che non ha mai usato il coupon
    }

    private boolean isItemEligibleForCoupon(Coupon coupon, List<ItemInPurchase> purchases) {
        // Verifica se almeno un item appartiene a una delle categorie specificate dal coupon
        return purchases.stream().anyMatch(item -> {
            if (item instanceof ProductInPurchase) {
                ProductInPurchase product = (ProductInPurchase) item;
                String categoryId = product.isFixed()
                        ? productFixedRepository.findById(product.getId()).map(ProductFixedAPA::getCategoryId).orElse(null)
                        : productKgRepository.findById(product.getId()).map(ProductKgAPA::getCategoryId).orElse(null);
                return coupon.getSpecificCategoriesId().contains(categoryId);
            } else if (item instanceof BundleInPurchase) {
                return trayRepository.findById(item.getId())
                        .map(tray -> coupon.getSpecificCategoriesId().contains(tray.getCategoryId()))
                        .orElse(false);
            }
            return false;
        });
    }


    private boolean isPriceInRange(List<ItemInPurchase> items, NumberRange range) {
        if (range == null) return true; // Nessun range da controllare, sempre valido

        return items != null && items.stream().anyMatch(item -> {
            if (item == null || item.getQuantity() <= 0) return false; // Evita divisioni per 0 o item nulli
            double unitPrice = item.getTotalPrice() / item.getQuantity();
            return isWithinRange(unitPrice, range);
        });
    }

    private boolean isWithinRange(double value, NumberRange range) {
        if (range == null) return true;
        // Verifica se il valore è compreso nel range
        boolean aboveMin = range.getMin() == null || value >= range.getMin();
        boolean belowMax = range.getMax() == null || value <= range.getMax();
        return aboveMin && belowMax;
    }

    private void applyCouponToPurchases(Coupon coupon, List<ItemInPurchase> purchases) {
        if (coupon == null || purchases == null || purchases.isEmpty()) return;

        if (coupon.getSpecificCategoriesId() == null || coupon.getSpecificCategoriesId().size() == 0) {
            // Applica il coupon sull'intero ordine
            applyCouponToEntireOrder(coupon, purchases);
        } else {
            // Applica il coupon solo agli item validi
            for (ItemInPurchase item : purchases) {
                if (isItemEligibleForCoupon(coupon, List.of(item))) {
                    applyDiscountToItem(coupon, item);
                }
            }
        }
    }

    private void applyCouponToEntireOrder(Coupon coupon, List<ItemInPurchase> purchases) {
        // Calcola il prezzo totale del carrello
        double totalPrice = purchases.stream()
                .mapToDouble(ItemInPurchase::getTotalPrice)
                .sum();

        // Calcola lo sconto
        double discount = 0;
        if (coupon instanceof PercentageCoupon) {
            discount = totalPrice * ((PercentageCoupon) coupon).getPercentage() / 100.0;
        } else if (coupon instanceof FixedAmountCoupon) {
            discount = Math.min(((FixedAmountCoupon) coupon).getFixedAmount(), totalPrice);
        }

        // Distribuisci lo sconto proporzionalmente agli articoli
        if (discount > 0) {
            double finalDiscount = discount; // Necessario per lambda
            purchases.forEach(item -> {
                double itemDiscount = (item.getTotalPrice() / totalPrice) * finalDiscount;
                item.applyDiscount(itemDiscount);
            });
        }
    }

    private void applyDiscountToItem(Coupon coupon, ItemInPurchase item) {
        if (coupon instanceof PercentageCoupon) {
            double discount = item.getTotalPrice() * ((PercentageCoupon) coupon).getPercentage() / 100.0;
            item.applyDiscount(discount);
        } else if (coupon instanceof FixedAmountCoupon) {
            double discount = Math.min(((FixedAmountCoupon) coupon).getFixedAmount(), item.getTotalPrice());
            item.applyDiscount(discount);
        }
    }

    private List<SummarySingleItemDTO> generateSummaryItems(List<ItemInPurchase> purchases) {
        List<SummarySingleItemDTO> summaryItems = new ArrayList<>();

        for (ItemInPurchase item : purchases) {
            SummarySingleItemDTO singleItem = new SummarySingleItemDTO();
            singleItem.setPrice(item.getTotalPrice());
            singleItem.setQuantity(item.getQuantity());

            if (item instanceof ProductInPurchase) {
                ProductKgAPA product = productKgRepository.findById(item.getId()).get();
                singleItem.setName(product.getName());
            }

            if (item instanceof BundleInPurchase) {
                Tray tray = trayRepository.findById(item.getId()).get();
                singleItem.setName(tray.getName());
            }

            summaryItems.add(singleItem);
        }

        return summaryItems;
    }

    private double calculateItemDiscount(Coupon coupon, ItemInPurchase item) {
        if (coupon instanceof PercentageCoupon) {
            return item.getTotalPrice() * ((PercentageCoupon) coupon).getPercentage() / 100.0;
        } else if (coupon instanceof FixedAmountCoupon) {
            return Math.min(((FixedAmountCoupon) coupon).getFixedAmount(), item.getTotalPrice());
        }
        return 0;
    }

    public double applyCouponToPurchasesAndCalculateDiscount(Coupon coupon, List<ItemInPurchase> purchases,boolean isPaypal) {
        if (coupon == null || purchases == null || purchases.isEmpty()) return 0;

        double totalDiscount = 0;

        if (coupon.getSpecificCategoriesId() == null || coupon.getSpecificCategoriesId().size() == 0) {
            // Applica il coupon sull'intero ordine
            double totalPrice = purchases.stream()
                    .mapToDouble(ItemInPurchase::getTotalPrice)
                    .sum();

            if (coupon instanceof PercentageCoupon) {
                totalDiscount = totalPrice * ((PercentageCoupon) coupon).getPercentage() / 100.0;
            } else if (coupon instanceof FixedAmountCoupon) {
                totalDiscount = Math.min(((FixedAmountCoupon) coupon).getFixedAmount(), totalPrice);
            }

            // Distribuisci lo sconto proporzionalmente agli articoli
            if(isPaypal){
                if (totalDiscount > 0) {
                    double finalDiscount = totalDiscount; // Necessario per lambda
                    purchases.forEach(item -> {
                        double itemDiscount = (item.getTotalPrice() / totalPrice) * finalDiscount;
                        item.applyDiscount(itemDiscount);
                    });
                }
            }

        } else {
            // Applica il coupon solo agli item validi
            for (ItemInPurchase item : purchases) {
                if (isItemEligibleForCoupon(coupon, List.of(item))) {
                    double itemDiscount = calculateItemDiscount(coupon, item);
                    item.applyDiscount(itemDiscount);
                    totalDiscount += itemDiscount;
                }
            }
        }

        return totalDiscount;
    }

    public Boolean sendCoupon(SendCouponReq sendCouponReq) throws IOException {
        emailService.sendCoupon(sendCouponReq);
        return true;
    }

    public HomeCouponDTO randomCouponWithHome() {
        List<Coupon> homeCoupons = couponRepository.findAllByExpiredFalseAndSoftDeletedFalseAndHomeIsNotNull();
        if (homeCoupons == null){
            return null;
        }
        Random random = new Random();
        int index = random.nextInt(homeCoupons.size());
        Coupon coupon = homeCoupons.get(index);

        HomeCouponDTO homeCoupon = new HomeCouponDTO();
        homeCoupon.setCode(coupon.getCode());
        homeCoupon.setHome(coupon.getHome());
        return homeCoupon;
    }
}
