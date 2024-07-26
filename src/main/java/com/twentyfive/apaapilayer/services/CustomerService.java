package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.clients.PaymentClientController;
import com.twentyfive.apaapilayer.dtos.*;
import com.twentyfive.apaapilayer.configurations.ProducerPool;
import com.twentyfive.apaapilayer.emails.EmailService;
import com.twentyfive.apaapilayer.exceptions.InvalidCategoryException;
import com.twentyfive.apaapilayer.exceptions.InvalidCustomerIdException;
import com.twentyfive.apaapilayer.exceptions.InvalidItemException;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.*;
import com.twentyfive.apaapilayer.utils.StompUtilities;
import com.twentyfive.apaapilayer.utils.TemplateUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.PaypalCredentials;
import twentyfive.twentyfiveadapter.dto.groypalDaemon.SimpleItem;
import twentyfive.twentyfiveadapter.dto.subscriptionDto.SimpleUnitAmount;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Customer;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final String NOTIFICATION_TOPIC="twentyfive_internal_notifications";

    private final ProductStatService productStatService;
    private final CustomerRepository customerRepository;

    private final CompletedOrderRepository completedOrderRepository;

    private final ActiveOrderService orderService;
    private final EmailService emailService;
    private final KeycloakService keycloakService;
    private final PaymentClientController paymentClientController;
    private final SettingRepository settingRepository;

    private final ProductKgRepository productKgRepository;
    private final ProductWeightedRepository productWeightedRepository;
    private final IngredientRepository ingredientRepository;
    private final AllergenRepository allergenRepository;
    private final TimeSlotAPARepository timeSlotAPARepository;

    private final CategoryRepository categoryRepository;

    private final TrayRepository trayRepository;
    private final ProducerPool producerPool;
    private final ActiveOrderRepository activeOrdersRepository;

    public CustomerDetailsDTO getCustomerDetailsByIdKeycloak(String idKeycloak) {
        CustomerAPA customer = customerRepository.findByIdKeycloak(idKeycloak)
                .orElseThrow(() -> new RuntimeException("No customer found with idKeycloak: " + idKeycloak));

        List<CompletedOrderAPA> completedOrders = completedOrderRepository.findByCustomerIdOrderByCreatedDateDesc(customer.getId());

        // Calcola il totale speso e il numero di ordini
        String totalSpent = String.format("%.2f", completedOrders.stream()
                .mapToDouble(CompletedOrderAPA::getTotalPrice)
                .sum());
        String completedOrdersCount = String.valueOf(completedOrders.size());

        List<OrderAPA> activeOrders = activeOrdersRepository.findByCustomerId(customer.getId());

        String activeOrdersCount = String.valueOf(activeOrders.size());


        return new CustomerDetailsDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getIdKeycloak(),
                customer.getEmail(),
                customer.getRole(),
                customer.getPhoneNumber(),
                completedOrdersCount,
                activeOrdersCount,
                totalSpent,
                customer.isEnabled(),
                customer.getNote()
        );
    }

    @Transactional
    public void modifyCustomerInfo(String customerId, String firstName, String lastName, String phoneNumber){

        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setPhoneNumber(phoneNumber);

        customerRepository.save(customer);
        System.out.println("ok");
        keycloakService.update(customer);

    }


    @Autowired
    public CustomerService(ProductStatService productStatService, ActiveOrderRepository activeOrderRepository, CustomerRepository customerRepository, ActiveOrderService activeOrderService, CompletedOrderRepository completedOrderRepository, EmailService emailService, KeycloakService keycloakService, PaymentClientController paymentClientController, SettingRepository settingRepository, ProductKgRepository productKgRepository, ProductWeightedRepository productWeightedRepository, IngredientRepository ingredientRepository, AllergenRepository allergenRepository, TimeSlotAPARepository timeSlotAPARepository, CategoryRepository categoryRepository, TrayRepository trayRepository, ProducerPool producerPool) {
        this.productStatService = productStatService;
        this.customerRepository = customerRepository;
        this.orderService = activeOrderService;
        this.completedOrderRepository=completedOrderRepository;
        this.emailService = emailService;
        this.keycloakService = keycloakService;
        this.paymentClientController = paymentClientController;
        this.settingRepository=settingRepository;
        this.productKgRepository = productKgRepository;
        this.productWeightedRepository = productWeightedRepository;
        this.ingredientRepository = ingredientRepository;
        this.allergenRepository = allergenRepository;
        this.timeSlotAPARepository = timeSlotAPARepository;
        this.categoryRepository = categoryRepository;
        this.trayRepository = trayRepository;
        this.producerPool = producerPool;
        this.activeOrdersRepository= activeOrderRepository;
    }

    public Page<CustomerAPA> getAllCustomers(int page, int size, String sortColumn, String sortDirection) {
        String customer="customer";
        Pageable pageable;
        if(!(sortDirection.isBlank() || sortColumn.isBlank())) {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            pageable=PageRequest.of(page,size,sort);
            return customerRepository.findAllByRoleAndIdKeycloakIsNotNull(customer,pageable);
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"lastName");
        pageable=PageRequest.of(page,size,sort);
        return customerRepository.findAllByRoleAndIdKeycloakIsNotNull(customer,pageable);
    }

    public Page<CustomerAPA> getAllEmployees(int page, int size, String sortColumn, String sortDirection) {
        List<String> excludedRoles = Arrays.asList("customer", "admin");
        Pageable pageable;
        if(!(sortDirection.isBlank() || sortColumn.isBlank())) {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            pageable=PageRequest.of(page,size,sort);
            return customerRepository.findAllByRoleNotInAndIdKeycloakIsNotNull(excludedRoles, pageable);
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"lastName");
        pageable=PageRequest.of(page,size,sort);
        return customerRepository.findAllByRoleNotInAndIdKeycloakIsNotNull(excludedRoles, pageable);
    }

    public CustomerDetailsDTO getById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());


        List<CompletedOrderAPA> comletedOrders = completedOrderRepository.findByCustomerIdOrderByCreatedDateDesc(customerId);

        // Calcola il totale speso e il numero di ordini
        String totalSpent = String.format("%.2f", comletedOrders.stream()
                .mapToDouble(CompletedOrderAPA::getTotalPrice)
                .sum());
        String completedOrdersCount = String.valueOf(comletedOrders.size());

        List<OrderAPA> activeOrders = activeOrdersRepository.findByCustomerId(customerId);

        String activeOrdersCount = String.valueOf(activeOrders.size());


        return new CustomerDetailsDTO(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getIdKeycloak(),
                customer.getEmail(),
                customer.getRole(),
                customer.getPhoneNumber(),
                completedOrdersCount,
                activeOrdersCount,
                totalSpent,
                customer.isEnabled(),
                customer.getNote()
            );


    }

    public CustomerAPA register(CustomerAPA customerAPA) {
        customerAPA.setRole("customer"); //Si possono registrare solo customer.
        return customerRepository.save(customerAPA);
    }

    public CustomerAPA saveCustomer(CustomerAPA customer) throws IOException {
        if(customer.getIdKeycloak()!=null){
            keycloakService.update(customer);
            return customerRepository.save(customer);
        } else {
            keycloakService.add(customer);
            keycloakService.sendPasswordResetEmail(customer.getIdKeycloak());
            return customerRepository.save(customer);
        }
    }

    public boolean changeStatusById(String id) {
        Optional<CustomerAPA> customerAPA = customerRepository.findById(id);
        // Verifica che il cliente esista prima di tentare di eliminarlo
        if (customerAPA.isPresent()) {
            customerAPA.get().setEnabled(!customerAPA.get().isEnabled());
            keycloakService.update(customerAPA.get());
            customerRepository.save(customerAPA.get());
        } else {
            // Se il cliente non esiste, restituisci false indicando che non c'era nulla da eliminare
            throw new InvalidCustomerIdException();
        }
        return true;
    }

    public CartDTO modifyProductInCart(String customerId, int index, ProductInPurchase pIP) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        cart.getPurchases().set(index,pIP);
        customerRepository.save(customer);
        return convertCartToDTO(customer);
    }

    public CartDTO modifyBundleInCart(String customerId, int index, BundleInPurchase bIP) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        cart.getPurchases().set(index,bIP);
        customerRepository.save(customer);
        return convertCartToDTO(customer);
    }

    public List<SummarySingleItemDTO> summary(String id, BuyInfosDTO buyInfos) {
        Optional<CustomerAPA> optCustomer = customerRepository.findById(id);
        List<SummarySingleItemDTO> summary = new ArrayList<>();
        if (optCustomer.isPresent()) {
            Cart cart = optCustomer.get().getCart();
            List<Integer> positions = buyInfos.getPositions();
            List<ItemInPurchase> selectedItems = cart.getItemsAtPositions(positions);
            for (Integer positionId : positions) {
                if (positionId < 0 || positionId >= cart.getPurchases().size()) {
                    throw new IndexOutOfBoundsException("Invalid item position: " + positionId);
                }
            }
            if (!selectedItems.isEmpty()) {
                for (ItemInPurchase item : selectedItems) {
                    SummarySingleItemDTO singleItem = new SummarySingleItemDTO();
                    singleItem.setPrice(item.getTotalPrice());
                    if (item instanceof ProductInPurchase){
                        ProductKgAPA product = productKgRepository.findById(item.getId()).get();
                        singleItem.setName(product.getName());
                    }
                    if (item instanceof BundleInPurchase){
                        Tray tray = trayRepository.findById(item.getId()).get();
                        singleItem.setName(tray.getName());
                    }
                    summary.add(singleItem);
                }
            }
        }
        return summary;
    }
    @Transactional
    public boolean buyItems(String customerId, BuyInfosDTO buyInfos) throws IOException {
        Optional<CustomerAPA> optCustomer = customerRepository.findById(customerId);
        String email="";
        String firstName="";
        if(optCustomer.isPresent()){
            CustomerAPA customer = optCustomer.get();
            TimeSlotAPA timeSlotAPA = timeSlotAPARepository.findAll().get(0);
            Cart cart = customer.getCart();
            List<Integer> positions = buyInfos.getPositions();
            List<ItemInPurchase> selectedItems = cart.getItemsAtPositions(positions);
            for(Integer positionId: positions){
                if (positionId < 0 || positionId >= cart.getPurchases().size()) {
                    throw new IndexOutOfBoundsException("Invalid item position: " + positionId);
                }
            }
            if (!selectedItems.isEmpty()) {
                OrderAPA order = createOrderFromItems(customer, buyInfos,selectedItems);
                if(timeSlotAPA.reserveTimeSlots(buyInfos.getSelectedPickupDateTime(),countSlotRequired(selectedItems))) {
                    orderService.createOrder(order);
                    cart.removeItemsAtPositions(buyInfos.getPositions()); // Rimuovi gli articoli dal carrello
                }
                timeSlotAPARepository.save(timeSlotAPA);
                customerRepository.save(customer);
                String in= StompUtilities.sendNewOrderNotification();
                producerPool.send(in,1,NOTIFICATION_TOPIC);
                if(buyInfos.getCustomInfo().getFirstName()!=null){
                    email = buyInfos.getCustomInfo().getEmail();
                    firstName = buyInfos.getCustomInfo().getFirstName();
                } else {
                    email = customer.getEmail();
                    firstName = customer.getFirstName();
                }
                emailService.sendEmail(email,OrderStatus.RICEVUTO, TemplateUtilities.populateEmail(firstName,order.getId()));
                return true;
            }
        }

        return false;
    }

    public Map<String,Object> prepareBuying(String id, String paymentAppId, PaymentReq paymentReq) {
        Optional<CustomerAPA> optCustomer = customerRepository.findById(id);
        if(optCustomer.isPresent()){
            CustomerAPA customer = optCustomer.get();
            Cart cart = customer.getCart();
            List<Integer> positions = paymentReq.getBuyInfos().getPositions();
            List<ItemInPurchase> selectedItems = cart.getItemsAtPositions(positions);
            for(Integer positionId: positions){
                if (positionId < 0 || positionId >= cart.getPurchases().size()) {
                    throw new IndexOutOfBoundsException("Invalid item position: " + positionId);
                }
            }
            List<SimpleItem> items = new ArrayList<>();
            double totalValue = 0;
            if (!selectedItems.isEmpty()) {
                for (ItemInPurchase selectedItem : selectedItems) {
                    SimpleItem simpleItem = new SimpleItem();
                    SimpleUnitAmount simpleUnitAmount = new SimpleUnitAmount();
                    String totalPrice = String.format(Locale.US,"%.2f", selectedItem.getTotalPrice()/selectedItem.getQuantity());
                    simpleUnitAmount.setValue(String.valueOf(totalPrice));
                    simpleUnitAmount.setCurrency_code("EUR");
                    String name ="";
                    String description ="";
                    if(selectedItem instanceof ProductInPurchase){
                        Optional<ProductKgAPA> optProductKg = productKgRepository.findById(selectedItem.getId());
                        if(optProductKg.isPresent()){
                            name = optProductKg.get().getName();
                            description = optProductKg.get().getDescription();
                        }
                    } else if (selectedItem instanceof BundleInPurchase) {
                        Optional<Tray> optTray = trayRepository.findById(selectedItem.getId());
                        if (optTray.isPresent()) {
                            name = optTray.get().getName();
                            description = optTray.get().getDescription();
                        }
                    }
                    simpleItem.setName(name);
                    simpleItem.setQuantity(String.valueOf(selectedItem.getQuantity()));
                    simpleItem.setDescription(description);
                    simpleItem.setUnit_amount(simpleUnitAmount);
                    totalValue +=selectedItem.getTotalPrice();
                    items.add(simpleItem);
                }
            }
            String authorization = keycloakService.getAccessTokenTF();
            paymentReq.getSimpleOrderRequest().setItems(items);
            String formattedValue = String.format(Locale.US,"%.2f", totalValue);
            paymentReq.getSimpleOrderRequest().setValue(formattedValue);
            PaypalCredentials paypalCredentials = settingRepository.findAll().get(0).getPaypalCredentials();
            paymentReq.getSimpleOrderRequest().setPaypalCredentials(paypalCredentials);

            return paymentClientController.pay(authorization,paymentReq.getSimpleOrderRequest(),paymentAppId).getBody();
        }
        return null;
    }
    private int countSlotRequired(List<ItemInPurchase>items) {
        int numSlotRequired = 0;


        for (ItemInPurchase item : items) {

            if (item instanceof ProductInPurchase) {
                ProductInPurchase pip = (ProductInPurchase) item;
                ProductKgAPA product = productKgRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                if (product.isCustomized()) {
                    numSlotRequired += pip.getQuantity();

                }


            } else if (item instanceof BundleInPurchase) {
                BundleInPurchase pip = (BundleInPurchase) item;
                Tray tray = trayRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                if (tray.isCustomized()) {
                    numSlotRequired += pip.getQuantity();
                }

            }
        }
        return numSlotRequired;
    }





    private OrderAPA createOrderFromItems(CustomerAPA customer,BuyInfosDTO buyInfos,List<ItemInPurchase> items) {
        OrderAPA order = new OrderAPA();
        if (buyInfos.getCustomInfo().getFirstName()!=null){
            order.setCustomInfo(buyInfos.getCustomInfo());
        } else {
            order.setCustomerId(customer.getId());
        }
        if(buyInfos.getPaymentId()!=null){
            order.setPaymentId(buyInfos.getPaymentId());
        }
        order.setPickupDate(buyInfos.getSelectedPickupDateTime().toLocalDate());
        order.setPickupTime(buyInfos.getSelectedPickupDateTime().toLocalTime());
        order.setStatus(OrderStatus.RICEVUTO);
        order.setNote(buyInfos.getNote());
        List<ProductInPurchase> products = new ArrayList<>();
        List<BundleInPurchase> bundles = new ArrayList<>();
        for (ItemInPurchase item : items) {
            if (item instanceof ProductInPurchase) {
                products.add((ProductInPurchase) item);
                Optional<ProductKgAPA> optProductKg =productKgRepository.findById(item.getId());
                if (optProductKg.isPresent()){
                    ProductKgAPA productKgAPA = optProductKg.get();
                    List<IngredientsWithCategory> ingredientsFromProduct = findIngredientsFromProduct(productKgAPA);
                    ((ProductInPurchase) item).setIngredients(ingredientsFromProduct);
                    productStatService.addBuyingCountProduct(productKgAPA, 1);
                    productKgRepository.save(productKgAPA);
                }
            } else if (item instanceof BundleInPurchase) {
                bundles.add((BundleInPurchase) item);
                Optional<Tray> tray=trayRepository.findById(item.getId());
                if(tray.isPresent()) {
                    productStatService.addBuyingCountTray(tray.get(),1);
                    for (PieceInPurchase piece : ((BundleInPurchase) item).getWeightedProducts()) {
                        Optional<ProductWeightedAPA> productWeightedAPA = productWeightedRepository.findById(piece.getId());
                        if (productWeightedAPA.isPresent()) {
                            productStatService.addBuyingCountProduct(productWeightedAPA.get(), 1);
                            productWeightedRepository.save(productWeightedAPA.get());
                        }
                    }
                }
            }
        }

        order.setProductsInPurchase(products);
        order.setBundlesInPurchase(bundles);
        order.setTotalPrice(calculateTotalPrice(items));

        return order;
    }

    private List<IngredientsWithCategory> findIngredientsFromProduct(ProductKgAPA productKgAPA) {
        List<IngredientsWithCategory> ingredients = new ArrayList<>();
        for (String ingredientId : productKgAPA.getIngredientIds()) {
            Optional<IngredientAPA> optIngredient = ingredientRepository.findById(ingredientId);
            if(optIngredient.isPresent()){
                IngredientAPA ingredient = optIngredient.get();
                Optional<CategoryAPA> optCategory = categoryRepository.findById(ingredient.getCategoryId());
                if(optCategory.isPresent()){
                    CategoryAPA category = optCategory.get();
                    boolean isPresent = false;
                    for (IngredientsWithCategory ingredientsWithCategory : ingredients) {
                        if(ingredientsWithCategory.getCategoryName().equals(category.getName())){
                            ingredientsWithCategory.getIngredientsName().add(ingredient.getName());
                            isPresent = true;
                            break;
                        }
                    }
                    if (!isPresent){
                        List<String> newCategoryIngredients = new ArrayList<>();
                        newCategoryIngredients.add(ingredient.getName());
                        IngredientsWithCategory ingredientsWithCategory = new IngredientsWithCategory(category.getName(),newCategoryIngredients);
                        ingredients.add(ingredientsWithCategory);
                    }
                }
            }
        }
        return ingredients;
    }

    private double calculateTotalPrice(List<ItemInPurchase> items) {
        double total = 0;
        for (ItemInPurchase item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public CartDTO getCartById(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) {
            customer.setCart(new Cart());
        }
        return convertCartToDTO(customer);
    }


    @Transactional
    public CartDTO removeFromCart(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        if (customer.getCart()==null) customer.setCart(new Cart());
        else {
            Cart cart=customer.getCart();
            cart.removeItemsAtPositions(positions);
            customerRepository.save(customer);
            return convertCartToDTO(customer);
        }
        throw new IllegalStateException("No cart available for this customer or invalid positions");
    }




    @Transactional
    public boolean clearCart(String customerId) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart= customer.getCart();
        if(cart==null)
            customer.setCart(new Cart());
        else {
            customer.getCart().clearCart();
            customerRepository.save(customer);
            return true;
        }
        return false;
    }

    @Transactional
    public CartDTO addToCartProduct(String customerId, ProductInPurchase product) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        ProductKgAPA productKg = productKgRepository.findById(product.getId()).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Cart cart = customer.getCart();
        Optional<ItemInPurchase> existingItem = cart.getPurchases().stream()
                .filter(pip -> pip.equals(product))
                .findFirst();

        if (existingItem.isPresent()) {
            ProductInPurchase existingProduct = (ProductInPurchase) existingItem.get();
            existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
            existingProduct.setTotalPrice(calculateTotalPrice(existingProduct, productKg.getPricePerKg()));
        } else {
            if(productKg.isCustomized()){
                List<Allergen> allergens = new ArrayList<>();
                for(Customization customization : product.getCustomization()){
                    for(String value: customization.getValue()){
                        Optional<IngredientAPA> optIngredient = ingredientRepository.findByName(value);
                        if(optIngredient.isPresent()){
                            IngredientAPA ingredient = optIngredient.get();
                            getAllergenFromIngredient(allergens,ingredient);
                        }
                    }
                }
                product.setAllergens(allergens);
            }
            product.setTotalPrice(calculateTotalPrice(product, productKg.getPricePerKg()));
            cart.getPurchases().add(product);
        }

        customerRepository.save(customer);
        return convertCartToDTO(customer);
    }

    @Transactional
    public CartDTO addToCartBundle(String customerId, BundleInPurchase bundle) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Tray tray = trayRepository.findById(bundle.getId()).orElseThrow(() -> new IllegalArgumentException("Tray not found"));
        Cart cart = customer.getCart();

        Optional<ItemInPurchase> existingItem = cart.getPurchases().stream()
                .filter(bip -> bip.equals(bundle))
                .findFirst();

        if (existingItem.isPresent()) {
            BundleInPurchase existingBundle = (BundleInPurchase) existingItem.get();
            existingBundle.setQuantity(existingBundle.getQuantity() + bundle.getQuantity());
            existingBundle.setTotalPrice(calculateTotalPrice(existingBundle, tray.getPricePerKg()));
        } else {
            if(tray.isCustomized()){
                List<Allergen> allergens = new ArrayList<>();
                for(PieceInPurchase piece : bundle.getWeightedProducts()){
                    Optional<ProductWeightedAPA> optPiece = productWeightedRepository.findById(piece.getId());
                    if(optPiece.isPresent()){
                        ProductWeightedAPA productWeighted = optPiece.get();
                        List<String> ingredentIds=productWeighted.getIngredientIds();
                        for (String id: ingredentIds){
                            IngredientAPA ingredient = ingredientRepository.findById(id).orElse(null);
                            List<String> allergenNames = ingredient.getAllergenNames();
                            for(String allergenName: allergenNames){
                                Allergen allergen = allergenRepository.findByName(allergenName).orElse(null);
                                if(allergen!=null && !allergens.contains(allergen))
                                    allergens.add(allergen);
                            }
                        }
                    }
                }
                bundle.setAllergens(allergens);
            }
            bundle.setTotalPrice(calculateTotalPrice(bundle, tray.getPricePerKg()));
            cart.getPurchases().add(bundle);
        }

        customerRepository.save(customer);
        return convertCartToDTO(customer);
    }

    public double calculateTotalPrice(ItemInPurchase item, double pricePerKg) {
        if (item instanceof ProductInPurchase) {
            ProductInPurchase product = (ProductInPurchase) item;
            double totalPrice;

            if (product.getTotalPrice() != 0) {
                totalPrice = product.getTotalPrice();
            } else {
                totalPrice = product.getQuantity() * (pricePerKg * product.getWeight());
            }

            if (product.getAttachment() != null && !product.getAttachment().isEmpty()) {
                totalPrice += 5;
            }
            return totalPrice;

        } else if (item instanceof BundleInPurchase) {
            BundleInPurchase bundle = (BundleInPurchase) item;
            return bundle.getQuantity() * (pricePerKg * bundle.getMeasure().getWeight());
        }
        throw new IllegalArgumentException("Unknown item type");
    }


    /*private LocalDateTime next(int hour){
        LocalDateTime now= LocalDateTime.now();
        // Definisce il mezzogiorno
        LocalTime noon = LocalTime.of(hour, 0);

        // Se ora è già passato il mezzogiorno, passa al giorno successivo
        if (now.toLocalTime().isAfter(noon)) {
            return now.toLocalDate().plusDays(1).atTime(noon);
        } else {
            // Altrimenti, restituisce il mezzogiorno di oggi
            return now.toLocalDate().atTime(noon);
        }
    }*/



    public Map<LocalDate, List<LocalTime>> getAvailablePickupTimes(String customerId, List<Integer> positions) {
        CustomerAPA customer = customerRepository.findById(customerId).orElseThrow(InvalidCustomerIdException::new);
        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();  // Assumi che Cart abbia un costruttore che inizializza le liste
            customer.setCart(cart);
            customerRepository.save(customer);
            return new TreeMap<>();
        } else {
            Integer minDelay = settingRepository.findAll().get(0).getMinOrderDelay();

            List<ItemInPurchase> items = cart.getItemsAtPositions(positions);
            int numSlotRequired = 0;
            boolean somethingCustomized = false;
            boolean bigSemifreddo = false;


            for (ItemInPurchase item : items) {

                if (item instanceof ProductInPurchase) {
                    ProductInPurchase pip = (ProductInPurchase) item;
                    ProductKgAPA product = productKgRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                    if (product.isCustomized()) {
                        numSlotRequired += pip.getQuantity();
                        somethingCustomized = true;

                    }
                    if (categoryRepository.findById(product.getCategoryId()).orElseThrow(InvalidCategoryException::new).getName().equals("Semifreddo")) {
                        double weight = pip.getWeight();
                        if (weight >= 1.5) bigSemifreddo = true;
                    }


                } else if (item instanceof BundleInPurchase) {
                    BundleInPurchase pip = (BundleInPurchase) item;
                    Tray tray = trayRepository.findById(pip.getId()).orElseThrow(InvalidItemException::new);
                    if (tray.isCustomized()) {
                        numSlotRequired += pip.getQuantity();
                        somethingCustomized = true;
                    }

                }

                LocalTime now = LocalTime.now();
                LocalTime startTime = settingRepository.findAll().get(0).getBusinessHours().getStartTime();
                ;
                LocalTime endTime = settingRepository.findAll().get(0).getBusinessHours().getEndTime();
                LocalDateTime minStartingDate;

                if (bigSemifreddo) minDelay = 48;

                if (!now.isBefore(startTime) && now.isBefore(endTime)) {//la richiesta è fatta in orario lavorativo
                    if (!somethingCustomized)
                        minStartingDate = LocalDateTime.now().plusHours(minDelay);
                    else
                        minStartingDate = next(8).plusHours(minDelay);
                } else {
                    if (!somethingCustomized)
                        minStartingDate = next(8).plusHours(minDelay);
                    else
                        minStartingDate = next(12).plusHours(minDelay);

                }


                Map<LocalDate, List<LocalTime>> availableTimes = timeSlotAPARepository.findAll().get(0).findTimeForNumSlots(minStartingDate, numSlotRequired);
                // Usa una TreeMap per garantire l'ordinamento per chiave (LocalDate)
                Map<LocalDate, List<LocalTime>> sortedAvailableTimes = new TreeMap<>(availableTimes);

                return sortedAvailableTimes;

            }
            return new TreeMap<>();


        }
    }


    private LocalDateTime next(int hour){
        LocalDateTime now= LocalDateTime.now();
        // Definisce il mezzogiorno
        LocalTime noon = LocalTime.of(hour, 0);

        // Se ora è già passato il mezzogiorno, passa al giorno successivo
        if (now.toLocalTime().isAfter(noon)) {
            return now.toLocalDate().plusDays(1).atTime(noon);
        } else {
            // Altrimenti, restituisce il mezzogiorno di oggi
            return now.toLocalDate().atTime(noon);
        }
    }


    private CartDTO convertCartToDTO(Customer customer){
        CartDTO cartDTO = new CartDTO();;
        cartDTO.setCustomerId(customer.getId());
        Double totalPrice=0.0;
        for (ItemInPurchase itemInPurchase : customer.getCart().getPurchases()){
            if (itemInPurchase instanceof BundleInPurchase){
                BundleInPurchaseDTO bundleInPurchaseDTO=convertBundlePurchaseToDTO((BundleInPurchase) itemInPurchase);
                cartDTO.getPurchases().add(bundleInPurchaseDTO);
                totalPrice+=bundleInPurchaseDTO.getTotalPrice();

            }
            if (itemInPurchase instanceof ProductInPurchase){
                ProductInPurchaseDTO productInPurchaseDTO =convertProductPurchaseToDTO((ProductInPurchase) itemInPurchase);
                cartDTO.getPurchases().add(productInPurchaseDTO);
                totalPrice+=productInPurchaseDTO.getTotalPrice();
            }
        }
        cartDTO.setTotalPrice(totalPrice);
        return cartDTO;
    }

    private ProductInPurchaseDTO convertProductPurchaseToDTO(ProductInPurchase productInPurchase) {
        Optional<ProductKgAPA> pKg = productKgRepository.findById(productInPurchase.getId());
        String name = pKg.map(ProductKgAPA::getName).orElse("no registered product");
        return new ProductInPurchaseDTO(productInPurchase, name);
    }

    private PieceInPurchaseDTO convertPiecePurchaseToDTO(PieceInPurchase piece) {
        Optional<ProductWeightedAPA> pWght = productWeightedRepository.findById(piece.getId());
        String name = pWght.map(ProductWeightedAPA::getName).orElse("No registered product");
        double weight = pWght.map(ProductWeightedAPA::getWeight).orElseThrow(() -> new IllegalArgumentException());
        return new PieceInPurchaseDTO(piece, name, weight);
    }
    private BundleInPurchaseDTO convertBundlePurchaseToDTO(BundleInPurchase bundleInPurchase) {
        Optional<Tray> bun = trayRepository.findById(bundleInPurchase.getId());
        String name = bun.map(Tray::getName).orElse("no registered product");
        if(bun.isPresent()){
            if (!bun.get().isCustomized()) {
                return new BundleInPurchaseDTO(bundleInPurchase,name);
            }
        }
        List<PieceInPurchase> weightedProducts= bundleInPurchase.getWeightedProducts();
        List<PieceInPurchaseDTO> weightedProductsDTOs= weightedProducts.stream()
                .map(this::convertPiecePurchaseToDTO) // Utilizza il metodo di conversione definito
                .collect(Collectors.toList());

        return new BundleInPurchaseDTO(bundleInPurchase, name,weightedProductsDTOs);
    }

    @Transactional
    public boolean deleteUser(String id) {
        Optional<CustomerAPA> optCustomer = customerRepository.findById(id);
        if (optCustomer.isPresent()){
            CustomerAPA customerAPA = optCustomer.get();
            keycloakService.deleteUser(customerAPA.getIdKeycloak());
            customerAPA.setIdKeycloak(null);
            customerRepository.save(customerAPA);
            return true;
        }
        return false;
    }

    private void getAllergenFromIngredient(List<Allergen> allergens,IngredientAPA ingredient){
        List<String> allergenNames = ingredient.getAllergenNames();
        for(String allergenName: allergenNames){
            Allergen allergen = allergenRepository.findByName(allergenName).orElse(null);
            if(allergen!=null && !allergens.contains(allergen))
                allergens.add(allergen);
        }
    }

    public boolean addFromCompletedOrder(String idCustomer,String idOrder) {
        Optional<CompletedOrderAPA> optOrder = completedOrderRepository.findById(idOrder);
        if (optOrder.isPresent()){
            CompletedOrderAPA order = optOrder.get();

            for (BundleInPurchase bIP : order.getBundlesInPurchase()) {
                addToCartBundle(idCustomer,bIP);
            }

            for (ProductInPurchase pIP : order.getProductsInPurchase()) {
                addToCartProduct(idCustomer,pIP);
            }
            return true;
        }
        return false;
    }

    public Map<String, Object> capture(String orderId) {
        String authorization=keycloakService.getAccessTokenTF();
        PaypalCredentials paypalCredentials = settingRepository.findAll().get(0).getPaypalCredentials();
        return paymentClientController.capture(authorization,orderId,paypalCredentials).getBody();
    }


}
