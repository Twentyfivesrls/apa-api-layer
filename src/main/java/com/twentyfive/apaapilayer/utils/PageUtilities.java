package com.twentyfive.apaapilayer.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class PageUtilities {
    // Cache to store class methods for performance improvement.
    private static final Map<Class<?>, Map<String, Method>> classMethodCache = new HashMap<>();


    public static <T> Page<T> convertListToPage(List<T> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public static <T> Page<T> convertListToPageWithSorting(List<T> list, Pageable pageable) {
        list = applySort(list, pageable.getSort());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    public static <T> Page<T> convertSetToPage(Set<T> set, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), set.size());

        List<T> list = new ArrayList<>(set);
        List<T> sublist = list.subList(start, end);

        return new PageImpl<>(sublist, pageable, set.size());
    }

    private static <T> List<T> applySort(List<T> list, Sort sort) {
        if (sort.isUnsorted()) {
            return list;
        }

        Comparator<T> comparator = null;

        for (Sort.Order order : sort) {
            Comparator<T> orderComparator = createComparator(order);
            if (comparator == null) {
                comparator = orderComparator;
            } else {
                comparator = comparator.thenComparing(orderComparator);
            }
        }

        return list.stream().sorted(comparator).collect(Collectors.toList());
    }

    private static <T> Comparator<T> createComparator(Sort.Order order) {
        return (o1, o2) -> {
            try {
                Comparable value1 = getComparablePropertyValue(o1, order.getProperty());
                Comparable value2 = getComparablePropertyValue(o2, order.getProperty());

                // Handle nulls to avoid NullPointerException
                if (value1 == null) return (value2 == null) ? 0 : -1;
                if (value2 == null) return 1;

                int comparison = value1.compareTo(value2);
                return order.isAscending() ? comparison : -comparison;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Reflection operation failed", e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparable getComparablePropertyValue(T object, String propertyName) throws ReflectiveOperationException {
        Method method = getMethod(object.getClass(), propertyName);
        return (Comparable) method.invoke(object);
    }

    private static Method getMethod(Class<?> clazz, String propertyName) throws NoSuchMethodException {
        Map<String, Method> methods = classMethodCache.computeIfAbsent(clazz, c -> new HashMap<>());
        String methodName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);

        return methods.computeIfAbsent(propertyName, prop -> {
            try {
                // Assuming a standard getter method with no parameters
                return clazz.getMethod(methodName);
            } catch (NoSuchMethodException e) {
                // Handle cases where the property might be a boolean and the getter starts with 'is'
                String isMethodName = "is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                try {
                    return clazz.getMethod(isMethodName);
                } catch (NoSuchMethodException ex) {
                    throw new RuntimeException("No getter method found for property: " + propertyName, ex);
                }
            }
        });
    }
}
