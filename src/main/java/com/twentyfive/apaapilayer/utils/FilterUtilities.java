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

import java.util.ArrayList;
import java.util.List;
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

    private static void addDateCriteria(Query query, OrderFilter filters) {
        Criteria dateCriteria = createDateCriteria(filters);
        if (dateCriteria != null) {
            query.addCriteria(dateCriteria);
        }
    }

    private static void addValueRange(Query query, Filter filters, String field) {
        Criteria valueRangeCriteria = createValueRangeCriteria(filters, field);
        if (valueRangeCriteria != null) {
            query.addCriteria(valueRangeCriteria);
        }
    }

    private static void addStatusCriteria(Query query, OrderFilter filters) {
        Criteria statusCriteria = createStatusCriteria(filters);
        if (statusCriteria != null) {
            query.addCriteria(statusCriteria);
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
}
