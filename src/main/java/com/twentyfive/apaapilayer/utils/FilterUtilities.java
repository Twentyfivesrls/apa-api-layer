package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.filters.Filter;
import com.twentyfive.apaapilayer.filters.IngredientFilter;
import com.twentyfive.apaapilayer.filters.OrderFilter;
import com.twentyfive.apaapilayer.filters.ProductFilter;
import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Product;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FilterUtilities {

    public static Query applyOrderFilters(Query query, OrderFilter filters, List<String> roles, CustomerRepository customerRepository) {
        if (roles.contains("baker")) {
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("productsInPurchase.toPrepare").is(true),
                    Criteria.where("bundlesInPurchase.toPrepare").is(true)
            ));
        }

        if (filters != null) {
            addDateCriteria(query, filters);
            addValueRange(query, filters,"totalPrice");
            addStatusCriteria(query, filters);
            addCustomerNameCriteria(query, filters, customerRepository);
        }

        return query;
    }

    public static Query applyIngredientFilters(Query query, IngredientFilter filters, String idCategory) {
        addCategoryId(query,idCategory);
        if (filters != null) {
            addName(query,filters);
            addAllergenName(query,filters);
        }
        return query;
    }

    public static <T> Query applyProductFilters(Query query, ProductFilter filters, String idCategory, IngredientRepository ingredientRepository, boolean checkSoftDeleted, Class<T> productClass) {
        addCategoryId(query,idCategory);
        addSoftDeleted(query,checkSoftDeleted);
        if (filters != null) {
            addName(query,filters);
            addIngredientName(query,filters,ingredientRepository);
            String fieldName=checkFieldNameByProductType(productClass);
            addValueRange(query,filters,fieldName);
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



    private static void addSoftDeleted(Query query,boolean checkSoftDeleted) {
        if (checkSoftDeleted) {
            query.addCriteria(Criteria.where("softDeleted").is(false));
        }
    }

    private static void addIngredientName(Query query, ProductFilter filters, IngredientRepository ingredientRepository) {
        if (filters.getIngredientNames()!=null) {
            // Trova gli ID degli ingredienti che corrispondono ai nomi specificati
            List<String> ingredientIds = ingredientRepository.findAllByNameIn(filters.getIngredientNames())
                    .stream()
                    .map(IngredientAPA::getId)
                    .collect(Collectors.toList());

            // Aggiungi un criterio per controllare che tutti questi ingredienti siano presenti in ingredientIds di ProductKg
            if (!ingredientIds.isEmpty()) {
                query.addCriteria(Criteria.where("ingredientIds").all(ingredientIds));
            }
        }
    }


    private static void addCategoryId(Query query, String idCategory) {
        query.addCriteria(Criteria.where("categoryId").is(idCategory));
    }

    private static void addAllergenName(Query query, IngredientFilter filters) {
        if(filters.getAllergenNames()!=null) {
            query.addCriteria(Criteria.where("allergenNames").all(filters.getAllergenNames()));
        }
    }

    private static void addName(Query query, Filter filters) {
        if (filters.getName()!=null) {
            query.addCriteria(Criteria.where("name").regex(filters.getName(), "i"));
        }
    }

    private static void addDateCriteria(Query query, OrderFilter filters) {
        if (filters.getDates() != null) {
            query.addCriteria(Criteria.where("pickupDate")
                    .gte(filters.getDates().getStartDate()));
            if(filters.getDates().getEndDate()!=null) {
                query.addCriteria(Criteria.where("pickupDate")
                        .lte(filters.getDates().getEndDate()));
            }
        }
    }

    private static void addValueRange(Query query, Filter filters, String field) {
        ValueRange values=extractFromFilter(filters);
        if (values != null) {
            Criteria priceCriteria = Criteria.where(field).gte(values.getMin());
            if (values.getMax() != null) {
                priceCriteria.lte(values.getMax());
            }
            query.addCriteria(priceCriteria);
        }
    }

    private static void addStatusCriteria(Query query, OrderFilter filters) {
        if (filters.getStatus() != null) {
            query.addCriteria(Criteria.where("status").is(filters.getStatus()));
        }
    }

    private static void addCustomerNameCriteria(Query query, OrderFilter filters, CustomerRepository customerRepository) {
        if (filters.getName() != null) {
            String searchTerm = filters.getName().replace(" ", ".*"); // Regex per coprire spazi tra parole

            // Criterio per customInfo se l'ordine è per un utente non registrato
            Criteria customInfoCriteria = new Criteria().orOperator(
                    Criteria.where("customInfo.firstName").regex(searchTerm, "i"),
                    Criteria.where("customInfo.lastName").regex(searchTerm, "i")
            );

            // Cerca nella repo solo se customInfo è null o non contiene il nome
            List<String> customerIds = customerRepository.findByFullNameOrFirstNameOrLastName(searchTerm)
                    .stream()
                    .map(CustomerAPA::getId)
                    .collect(Collectors.toList());

            Criteria customerIdCriteria = Criteria.where("customerId").in(customerIds);

            // Aggiungi i criteri sia per customerId che per customInfo
            query.addCriteria(new Criteria().orOperator(customInfoCriteria, customerIdCriteria));
        }
    }

    private static ValueRange extractFromFilter(Filter filters){
        if(filters instanceof ProductFilter) {
            if(((ProductFilter) filters).getValues()!=null){
                return ((ProductFilter) filters).getValues();
            }
        } else if(filters instanceof OrderFilter) {
            if (((OrderFilter) filters).getValues()!=null){
                return ((OrderFilter) filters).getValues();
            }
        }
        return null;
    }
}
