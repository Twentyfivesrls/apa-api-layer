package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.models.OrderFilter;
import com.twentyfive.apaapilayer.models.ProductFilter;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.CustomerRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
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
            addPriceCriteria(query, filters);
            addStatusCriteria(query, filters);
            addCustomerNameCriteria(query, filters, customerRepository);
        }

        return query;
    }


    public static Query applyProductFilters(Query query, ProductFilter filters,String idCategory) {
        addCategoryId(query,idCategory);
        if (filters != null) {
            addProductName(query,filters);
            addAllergenName(query,filters);
        }

        return query;
    }

    private static void addCategoryId(Query query, String idCategory) {
        query.addCriteria(Criteria.where("categoryId").is(idCategory));
    }

    private static void addAllergenName(Query query, ProductFilter filters) {
        if (filters.getAllergenNames() != null) {
            query.addCriteria(Criteria.where("allergenNames").all(filters.getAllergenNames()));
        }
    }

    private static void addProductName(Query query, ProductFilter filters) {
        if (filters.getName()!=null) {
            query.addCriteria(Criteria.where("name").regex(filters.getName(), "i"));
        }
    }

    private static void addDateCriteria(Query query, OrderFilter filters) {
        if (filters.getDates() != null) {
            query.addCriteria(Criteria.where("pickupDate")
                    .gte(filters.getDates().getStartDate())
                    .lte(filters.getDates().getEndDate()));
        }
    }

    private static void addPriceCriteria(Query query, OrderFilter filters) {
        if (filters.getValues() != null) {
            query.addCriteria(Criteria.where("totalPrice")
                    .gte(filters.getValues().getMin())
                    .lte(filters.getValues().getMax()));
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

}
