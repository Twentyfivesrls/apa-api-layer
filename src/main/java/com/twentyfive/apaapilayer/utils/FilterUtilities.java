package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.filters.*;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Product;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.CouponType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterUtilities {

    public static Query applyOrderFilters(Query query, OrderFilter filters, List<String> roles, CustomerRepository customerRepository) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Se il ruolo è "baker", aggiungiamo il criterio relativo
        if (roles.contains("baker")) {
            criteriaList.add(createBakerCriteria());
        }

        if (filters != null) {
            Criteria dateCriteria = createDateCriteria(filters);
            if (dateCriteria != null) criteriaList.add(dateCriteria);

            Criteria valueRangeCriteria = createValueRangeCriteria(filters, "totalPrice");
            if (valueRangeCriteria != null) criteriaList.add(valueRangeCriteria);

            Criteria statusCriteria = createStatusCriteria(filters);
            if (statusCriteria != null) criteriaList.add(statusCriteria);

            Criteria customerNameCriteria = createCustomerNameCriteria(filters, customerRepository);
            if (customerNameCriteria != null) criteriaList.add(customerNameCriteria);
        }

        // Combiniamo tutti i criteri in un unico andOperator
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    public static Query applyCouponFilters(Query query, CouponFilter filters) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (filters != null) {
            Criteria expiredCriteria = createExpiredCriteria(filters);
            if (expiredCriteria != null) criteriaList.add(expiredCriteria);

            Criteria dateCriteria = createCouponDateCriteria(filters);
            if (dateCriteria != null) criteriaList.add(dateCriteria);

            Criteria typeCriteria = createTypeCriteria(filters);
            if (typeCriteria != null) criteriaList.add(typeCriteria);

            Criteria priceCriteria = createPriceCriteria(filters);
            if (priceCriteria != null) criteriaList.add(priceCriteria);

            Criteria nameCriteria = createCouponNameCriteria(filters);
            if (nameCriteria != null) criteriaList.add(nameCriteria);

        }

        // Combiniamo tutti i criteri in un unico andOperator
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    private static Criteria createCouponNameCriteria(CouponFilter filters) {
        if (filters != null){
            if (filters.getName() != null) {
                return Criteria.where("name").regex(Pattern.quote(filters.getName()), "i");
            }
        }
        return null;
    }

    private static Criteria createTypeCriteria(CouponFilter filters) {
        if (filters != null) {
            if (filters.getType() != null) {
                CouponType type = CouponType.valueOf(filters.getType());
                if (type == CouponType.FIXED){
                    return Criteria.where("_class").is("twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.FixedAmountCoupon");
                } else if (type == CouponType.PERCENTAGE){
                    return Criteria.where("_class").is("twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.PercentageCoupon");
                }
            }
        }
        return null;
    }

    private static Criteria createExpiredCriteria(CouponFilter filters) {
        if (filters != null && filters.getExpired() != null) {
            return Criteria.where("expired").is(filters.getExpired());
        }
        return null;
    }

    private static Criteria createBakerCriteria() {
        return new Criteria().orOperator(
                Criteria.where("productsInPurchase.toPrepare").is(true),
                Criteria.where("bundlesInPurchase.toPrepare").is(true)
        );
    }

    private static Criteria createDateCriteria(OrderFilter filters) {
        if (filters.getDates() != null) {
            Criteria dateCriteria = Criteria.where("pickupDate");
            if (filters.getDates().getStartDate() != null) {
                dateCriteria = dateCriteria.gte(filters.getDates().getStartDate());
            }
            if (filters.getDates().getEndDate() != null) {
                dateCriteria = dateCriteria.lte(filters.getDates().getEndDate());
            }
            return dateCriteria;
        }
        return null;
    }

    private static Criteria createCouponDateCriteria(CouponFilter filters) {
        if (filters.getDates() == null) {
            return null; // Nessun filtro da applicare
        }

        LocalDate startFilter = filters.getDates().getStartDate();
        LocalDate endFilter = filters.getDates().getEndDate();

        Criteria dateCriteria = new Criteria();

        if (startFilter != null || endFilter != null) {
            dateCriteria = Criteria.where("dates").elemMatch(new Criteria().orOperator(
                    // Caso 1: Il coupon ha entrambi startDate e endDate e si sovrappone al range
                    new Criteria().andOperator(
                            Criteria.where("startDate").lte(endFilter != null ? endFilter : LocalDate.MAX),
                            Criteria.where("endDate").gte(startFilter != null ? startFilter : LocalDate.MIN)
                    ),
                    // Caso 2: Il coupon ha solo startDate ed è valido da lì in poi
                    Criteria.where("startDate").lte(endFilter != null ? endFilter : LocalDate.MAX)
                            .and("endDate").exists(false),
                    // Caso 3: Il coupon ha solo endDate ed è valido fino a quella data
                    Criteria.where("endDate").gte(startFilter != null ? startFilter : LocalDate.MIN)
                            .and("startDate").exists(false),
                    // Caso 4: Il coupon ha startDate o endDate mancanti (senza restrizioni)
                    Criteria.where("startDate").exists(false), // Senza startDate è valido da sempre
                    Criteria.where("endDate").exists(false)  // Senza endDate è valido per sempre
            ));
        }

        // Includi sempre i documenti che non hanno proprio il campo `dates`
        return new Criteria().orOperator(
                dateCriteria,
                Criteria.where("dates").exists(false)
        );
    }


    private static Criteria createValueRangeCriteria(Filter filters, String field) {
        ValueRange values = extractFromFilter(filters);
        if (values != null) {
            Criteria criteria = Criteria.where(field);
            if (values.getMin() != null) {
                criteria = criteria.gte(values.getMin());
            }
            if (values.getMax() != null) {
                criteria = criteria.lte(values.getMax());
            }
            return criteria;
        }
        return null;
    }

    private static Criteria createStatusCriteria(OrderFilter filters) {
        if (filters.getStatus() != null) {
            return Criteria.where("status").is(filters.getStatus());
        }
        return null;
    }

    private static Criteria createCustomerNameCriteria(OrderFilter filters, CustomerRepository customerRepository) {
        if (filters.getName() != null) {
            String searchTerm = filters.getName().replace(" ", ".*"); // Regex per coprire spazi tra parole

            // Criterio per customInfo se l'ordine è per un utente non registrato
            Criteria customInfoCriteria = new Criteria().orOperator(
                    Criteria.where("customInfo.firstName").regex(searchTerm, "i"),
                    Criteria.where("customInfo.lastName").regex(searchTerm, "i")
            );

            // Cerca nella repo gli ID dei clienti che matchano il nome
            List<String> customerIds = customerRepository.findByFullNameOrFirstNameOrLastName(searchTerm)
                    .stream()
                    .map(CustomerAPA::getId)
                    .collect(Collectors.toList());

            Criteria customerIdCriteria = Criteria.where("customerId").in(customerIds);

            // Combiniamo i due criteri con un orOperator
            return new Criteria().orOperator(customInfoCriteria, customerIdCriteria);
        }
        return null;
    }

    public static Query applyIngredientFilters(Query query, IngredientFilter filters, String idCategory) {
        addCategoryId(query, idCategory);
        if (filters != null) {
            addName(query, filters);
            addAllergenName(query, filters);
        }
        return query;
    }

    public static <T> Query applyProductFilters(Query query, ProductFilter filters, String idCategory,
                                                IngredientRepository ingredientRepository, boolean checkSoftDeleted, Class<T> productClass) {
        addCategoryId(query, idCategory);
        addSoftDeleted(query, checkSoftDeleted);
        if (filters != null) {
            addName(query, filters);
            addIngredientName(query, filters, ingredientRepository);
            String fieldName = checkFieldNameByProductType(productClass);
            addValueRange(query, filters, fieldName);
        }
        return query;
    }

    private static <T> String checkFieldNameByProductType(Class<T> productClass) {
        if (productClass.equals(ProductKgAPA.class)) {
            return "pricePerKg";
        } else if (productClass.equals(ProductWeightedAPA.class)) {
            return "weight";
        } else if (productClass.equals(ProductFixedAPA.class)) {
            return "price";
        }
        return null;
    }

    private static void addSoftDeleted(Query query, boolean checkSoftDeleted) {
        if (checkSoftDeleted) {
            query.addCriteria(Criteria.where("softDeleted").is(false));
        }
    }

    private static void addIngredientName(Query query, ProductFilter filters, IngredientRepository ingredientRepository) {
        if (filters.getIngredientNames() != null) {
            List<String> ingredientIds = ingredientRepository.findAllByNameIn(filters.getIngredientNames())
                    .stream()
                    .map(IngredientAPA::getId)
                    .collect(Collectors.toList());

            if (!ingredientIds.isEmpty()) {
                query.addCriteria(Criteria.where("ingredientIds").all(ingredientIds));
            }
        }
    }

    private static void addCategoryId(Query query, String idCategory) {
        query.addCriteria(Criteria.where("categoryId").is(idCategory));
    }

    private static void addAllergenName(Query query, IngredientFilter filters) {
        if (filters.getAllergenNames() != null) {
            query.addCriteria(Criteria.where("allergenNames").all(filters.getAllergenNames()));
        }
    }

    private static void addName(Query query, Filter filters) {
        if (filters.getName() != null) {
            query.addCriteria(Criteria.where("name").regex(filters.getName(), "i"));
        }
    }

    private static void addValueRange(Query query, Filter filters, String field) {
        Criteria valueRangeCriteria = createValueRangeCriteria(filters, field);
        if (valueRangeCriteria != null) {
            query.addCriteria(valueRangeCriteria);
        }
    }

    private static ValueRange extractFromFilter(Filter filters) {
        if (filters instanceof ProductFilter) {
            if (((ProductFilter) filters).getValues() != null) {
                return ((ProductFilter) filters).getValues();
            }
        } else if (filters instanceof OrderFilter) {
            if (((OrderFilter) filters).getValues() != null) {
                return ((OrderFilter) filters).getValues();
            }
        }
        return null;
    }

    private static Criteria createPriceCriteria(CouponFilter filters) {
        if (filters.getValues() == null) {
            return null; // Nessun filtro sui prezzi
        }

        Double minFilter = filters.getValues().getMin();
        Double maxFilter = filters.getValues().getMax();

        Criteria priceCriteria = new Criteria();

        if (minFilter != null || maxFilter != null) {
            priceCriteria = Criteria.where("priceRange").elemMatch(new Criteria().orOperator(
                    // Caso 1: Il coupon ha entrambi min e max e il prezzo rientra nel range
                    new Criteria().andOperator(
                            Criteria.where("min").lte(maxFilter != null ? maxFilter : Double.MAX_VALUE),
                            Criteria.where("max").gte(minFilter != null ? minFilter : Double.MIN_VALUE)
                    ),
                    // Caso 2: Il coupon ha solo min e il prezzo è maggiore o uguale a min
                    Criteria.where("min").lte(maxFilter != null ? maxFilter : Double.MAX_VALUE)
                            .and("max").exists(false),
                    // Caso 3: Il coupon ha solo max e il prezzo è minore o uguale a max
                    Criteria.where("max").gte(minFilter != null ? minFilter : Double.MIN_VALUE)
                            .and("min").exists(false),
                    // Caso 4: Il coupon non ha min o max
                    Criteria.where("min").exists(false),
                    Criteria.where("max").exists(false)
            ));
        }

        // Includi sempre i documenti che non hanno proprio il campo `priceRange`
        return new Criteria().orOperator(
                priceCriteria,
                Criteria.where("priceRange").exists(false)
        );
    }

}
