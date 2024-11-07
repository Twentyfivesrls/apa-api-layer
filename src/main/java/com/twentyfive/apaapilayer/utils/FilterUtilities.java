package com.twentyfive.apaapilayer.utils;

import com.twentyfive.apaapilayer.models.CustomerAPA;
import com.twentyfive.apaapilayer.models.OrderFilter;
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
            List<String> customerIds = customerRepository.findByFullNameOrFirstNameOrLastName(searchTerm)
                    .stream()
                    .map(CustomerAPA::getId)
                    .collect(Collectors.toList());
            query.addCriteria(Criteria.where("customerId").in(customerIds));
        }
    }
}
