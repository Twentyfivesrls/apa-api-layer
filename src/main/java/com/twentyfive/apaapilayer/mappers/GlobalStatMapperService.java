package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.stats.*;
import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import com.twentyfive.apaapilayer.repositories.CompletedOrderRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.StatLabel;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.stat.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.time.LocalDate;
import java.util.*;
@Service
@RequiredArgsConstructor
public class GlobalStatMapperService {

    private final CompletedOrderRepository completedOrderRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;

    public GlobalStatAPA createGlobalStatByDate(LocalDate date) {
        GlobalStatAPA globalStat = new GlobalStatAPA();
        globalStat.setProducts(createGlobalProductStatByDate(date));
        globalStat.setIngredients(createGlobalIngredientStatByDate(date));
        return globalStat;
    }

    private GlobalIngredientStat createGlobalIngredientStatByDate(LocalDate date) {
        GlobalIngredientStat globalIngredientStat = new GlobalIngredientStat();
        globalIngredientStat.setGeneralStat(createGeneralIngredientStatByDate(date));
        globalIngredientStat.setCategoryStat(createGlobalCategoryStats(date));
        globalIngredientStat.setCategoryStats(createIngredientCategoryStat(date));
        return globalIngredientStat;
    }

    private List<CategoryIngredientStat> createIngredientCategoryStat(LocalDate date) { //todo non punziona
        List<String> ingredientIds = completedOrderRepository.findDistinctIngredientIds(date,OrderStatus.COMPLETO);
        List<CategoryIngredientStat> categoryIngredientStats = new ArrayList<>();
        for (String ingredientId : ingredientIds) {
            CategoryIngredientStat categoryIngredientStat = new CategoryIngredientStat();
            categoryIngredientStat.setIdIngredient(ingredientId);
            categoryIngredientStat.setTotalTimeUsed(completedOrderRepository.countIngredientUsage(date,OrderStatus.COMPLETO,ingredientId).orElse(0L));
            categoryIngredientStats.add(categoryIngredientStat);
        }
        return categoryIngredientStats;
    }

    private List<GlobalCategoryStat> createGlobalCategoryStats(LocalDate date) {
        List<String> categoryIds = completedOrderRepository.findDistinctCategoryIdsForIngredientsByDateAndStatus(date,OrderStatus.COMPLETO);
        List<GlobalCategoryStat> globalCategoryStats = new ArrayList<>();
        for (String categoryId : categoryIds) {
            GlobalCategoryStat globalCategoryStat = createGlobalCategoryStat(date,categoryId);
            globalCategoryStats.add(globalCategoryStat);
        }
        return globalCategoryStats;
    }

    private GlobalCategoryStat createGlobalCategoryStat(LocalDate date, String categoryId) {
        GlobalCategoryStat globalCategoryStat = new GlobalCategoryStat();
        globalCategoryStat.setIdCategory(categoryId);
        globalCategoryStat.setUsedIngredients(completedOrderRepository.countDistinctIngredients(date,OrderStatus.COMPLETO).orElse(0L));
        globalCategoryStat.setTotalUsedIngredients(completedOrderRepository.countTotalIngredientsByCategory(date,OrderStatus.COMPLETO,categoryId).orElse(0L));
        return globalCategoryStat;
    }

    private GeneralIngredientStat createGeneralIngredientStatByDate(LocalDate date) {
        GeneralIngredientStat generalIngredientStat = new GeneralIngredientStat();
        generalIngredientStat.setTotalIngredients(ingredientRepository.count());
        generalIngredientStat.setUsedIngredients(completedOrderRepository.countUniqueIngredients(date,OrderStatus.COMPLETO).orElse(0L));
        generalIngredientStat.setTotalUsedIngredients(completedOrderRepository.countTotalIngredients(date,OrderStatus.COMPLETO).orElse(0L)); //TODO it doesn't work
        return generalIngredientStat;
    }

    private GlobalProductStat createGlobalProductStatByDate(LocalDate date) {
        List<String> categoryIds = completedOrderRepository.findDistinctCategoryIdsByDateAndStatus(date,OrderStatus.COMPLETO);
        GlobalProductStat globalProductStat = new GlobalProductStat();
        globalProductStat.setGeneralStat(createGeneralProductStatByDate(date));
        globalProductStat.setCustomCakeStat(createCustomStatByDate(date));
        globalProductStat.setDashboardProductStats(createDashboardProductStatsByDate(date,categoryIds));
        globalProductStat.setCategoryStats(createCategoryStatsByDate(date,categoryIds));
        return globalProductStat;
    }

    private List<CategoryProductStat> createCategoryStatsByDate(LocalDate date, List<String> categoryIds) {
        List<CategoryProductStat> categoryProductStats = new ArrayList<>();

        for (String categoryId : categoryIds) {
            CategoryProductStat categoryProductStat = new CategoryProductStat();
            categoryProductStat.setIdCategory(categoryId);
            updateCategoryStatByDate(date, categoryProductStat, categoryId);
            categoryProductStats.add(categoryProductStat);
        }
        return categoryProductStats;
    }

    private void updateCategoryStatByDate(LocalDate date, CategoryProductStat categoryProductStat, String categoryId) {
        categoryProductStat.setProductStats(createProductStatByDate(date,categoryId));
    }

    private List<ProductStatCategory> createProductStatByDate(LocalDate date, String categoryId) {
        List<ProductStatCategory> productStatCategories = new ArrayList<>();
        Set<String> productIdsByCategoryId = completedOrderRepository.findProductIdsByCategoryAndStatus(categoryId, OrderStatus.COMPLETO, date);
        for (String productId : productIdsByCategoryId) {
            ProductStatCategory productStatCategory = new ProductStatCategory();
            productStatCategory.setIdProduct(productId);
            productStatCategory.setQuantity(completedOrderRepository.sumQuantityByProductId(date,OrderStatus.COMPLETO,productId).orElse(0L));
            productStatCategory.setTotalRevenue(completedOrderRepository.sumTotalPriceByProductId(date,OrderStatus.COMPLETO,productId).orElse(0.0));
            productStatCategory.setTotalWeight(completedOrderRepository.sumTotalWeightByProductId(date,OrderStatus.COMPLETO,productId).orElse(0.0));
            productStatCategories.add(productStatCategory);
        }
        return productStatCategories;
    }

    private List<DashboardProductStat> createDashboardProductStatsByDate(LocalDate date,List<String> categoryIds) {
        List<DashboardProductStat> dashboardProductStats = new ArrayList<>();

        for (String categoryId : categoryIds) {
            DashboardProductStat dashboardProductStat = createDashboardProductStatByDate(date,categoryId);
            dashboardProductStats.add(dashboardProductStat);
        }

        return dashboardProductStats;


    }

    private DashboardProductStat createDashboardProductStatByDate(LocalDate date,String categoryId) {
        DashboardProductStat dashboardProductStat = new DashboardProductStat();
        dashboardProductStat.setIdCategory(categoryId);
        dashboardProductStat.setTotalRevenue(completedOrderRepository.sumTotalPriceByCategoryId(date,OrderStatus.COMPLETO,categoryId).orElse(0.0));
        dashboardProductStat.setTotalProductSold(completedOrderRepository.sumQuantityByCategoryId(date,OrderStatus.COMPLETO,categoryId).orElse(0L));
        return dashboardProductStat;
    }

    private CustomCakeStat createCustomStatByDate(LocalDate date) {
        CustomCakeStat customCakeStat = new CustomCakeStat(createStatLabels(date));
        return customCakeStat;
    }

    private List<StatLabel> createStatLabels(LocalDate date) {
        List<StatLabel> statLabels = new ArrayList<>(4);



        StatLabel personalizedCake = new StatLabel();
        personalizedCake.setLabel("Personalizzate");
        personalizedCake.setTotal(completedOrderRepository.sumQuantityByPickupDateStatusAndProductId(date,OrderStatus.COMPLETO,"6679566c03d8511e7a0d449c").orElse(0L));
        personalizedCake.setValue(completedOrderRepository.sumTotalPriceByPickupDateStatusAndProductId(date,OrderStatus.COMPLETO,"6679566c03d8511e7a0d449c").orElse(0.0));
        statLabels.add(personalizedCake);

        StatLabel highCakeStat = new StatLabel();
        highCakeStat.setLabel("Alte");
        highCakeStat.setTotal(completedOrderRepository.sumQuantityByCategoryId(date,OrderStatus.COMPLETO,"6717a7d453e7c62ae1fb38e9").orElse(0L));
        highCakeStat.setValue(completedOrderRepository.sumTotalPriceByCategoryId(date,OrderStatus.COMPLETO,"6717a7d453e7c62ae1fb38e9").orElse(0.0));
        statLabels.add(highCakeStat);

        StatLabel planCakeStat = new StatLabel();
        planCakeStat.setLabel("A Piani");
        planCakeStat.setTotal(completedOrderRepository.sumQuantityByCategoryId(date,OrderStatus.COMPLETO,"670d2da65ab21a706dfa3c58").orElse(0L));
        planCakeStat.setValue(completedOrderRepository.sumTotalPriceByCategoryId(date,OrderStatus.COMPLETO,"670d2da65ab21a706dfa3c58").orElse(0.0));
        statLabels.add(planCakeStat);

        StatLabel customCakeStat = new StatLabel();
        customCakeStat.setLabel("Torte custom vendute");
        customCakeStat.setTotal(personalizedCake.getTotal()+highCakeStat.getTotal()+planCakeStat.getTotal());
        customCakeStat.setValue(personalizedCake.getValue()+highCakeStat.getValue()+planCakeStat.getValue());
        statLabels.add(customCakeStat);

        return statLabels;
    }

    private GeneralProductStat createGeneralProductStatByDate(LocalDate date) {
        GeneralProductStat generalProductStat = new GeneralProductStat();
        generalProductStat.setTotalOrders(completedOrderRepository.countByPickupDateAndStatus(date,"COMPLETO"));
        generalProductStat.setTotalRevenue(completedOrderRepository.calculateTotalPriceByPickupDateAndStatus(date, OrderStatus.COMPLETO).orElse(0.0));
        generalProductStat.setTotalCustomersServed(completedOrderRepository.countDistinctCustomerIdByPickupDateAndStatus(date, OrderStatus.COMPLETO).orElse(0L));
        generalProductStat.setTotalProductsSold(completedOrderRepository.sumQuantitiesByPickupDateAndStatus(date, OrderStatus.COMPLETO).orElse(0L));
        return generalProductStat;
    }

    public GlobalStatDTO createGlobalStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GlobalStatDTO globalStat = new GlobalStatDTO();
        globalStat.setGlobalProductStat(createGlobalProductStatDTOFromGlobalStat(globalStats));
        globalStat.setGlobalIngredientStat(createGlobalIngredientStatDTOFromGlobalStat(globalStats));
        return globalStat;
    }

    private GlobalIngredientStatDTO createGlobalIngredientStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GlobalIngredientStatDTO globalIngredientStatDTO = new GlobalIngredientStatDTO();
        globalIngredientStatDTO.setGeneralStat(sumIngredientGeneralStatsFromGlobalStatList(globalStats));
        globalIngredientStatDTO.setGlobalCategoryStats(createListGlobalCategoryStatDTOFromListGlobalCategoryStat(globalStats));
        return globalIngredientStatDTO;
    }

    private List<GlobalCategoryStatDTO> createListGlobalCategoryStatDTOFromListGlobalCategoryStat(List<GlobalStatAPA> globalStats) {
        Map<String,GlobalCategoryStatDTO> globalCategoryStats = new HashMap<>(globalStats.size());
        for (GlobalStatAPA globalStat : globalStats) {
            createOrUploadKeyForGlobalCategoryStats(globalStat,globalCategoryStats);
        }
        return new ArrayList<>(globalCategoryStats.values());
    }

    private void createOrUploadKeyForGlobalCategoryStats(GlobalStatAPA globalStat, Map<String, GlobalCategoryStatDTO> globalCategoryStats) {
        for (GlobalCategoryStat globalCategoryStat : globalStat.getIngredients().getCategoryStat()) {
            if (globalCategoryStats.containsKey(globalCategoryStat.getIdCategory())){
                GlobalCategoryStatDTO globalCategoryStatDTO = globalCategoryStats.get(globalCategoryStat.getIdCategory());
                globalCategoryStatDTO.setUsedIngredients(globalCategoryStatDTO.getUsedIngredients()+globalCategoryStat.getUsedIngredients());
                globalCategoryStatDTO.setTotalUsedIngredients(globalCategoryStatDTO.getTotalUsedIngredients()+globalCategoryStat.getTotalUsedIngredients());
            } else {
                String name = categoryRepository.findById(globalCategoryStat.getIdCategory()).get().getName();
                GlobalCategoryStatDTO globalCategoryStatDTO = new GlobalCategoryStatDTO();
                globalCategoryStatDTO.setIdCategory(globalCategoryStat.getIdCategory());
                globalCategoryStatDTO.setName(name);
                globalCategoryStatDTO.setUsedIngredients(globalCategoryStat.getUsedIngredients());
                globalCategoryStatDTO.setTotalUsedIngredients(globalCategoryStat.getTotalUsedIngredients());
            }

        }
    }

    private void createOrUploadKeyForDashboardStats(GlobalStatAPA globalStatAPA, Map<String, DashboardProductStatDTO> dashboardStats) {
        for (DashboardProductStat dashboardProductStat : globalStatAPA.getProducts().getDashboardProductStats()) {
            if (dashboardStats.containsKey(dashboardProductStat.getIdCategory())){
                DashboardProductStatDTO dashboardProductStatDTO = dashboardStats.get(dashboardProductStat.getIdCategory());
                dashboardProductStatDTO.setTotalRevenue(dashboardProductStatDTO.getTotalRevenue()+dashboardProductStat.getTotalRevenue());
                dashboardProductStatDTO.setTotalProductSold(dashboardProductStatDTO.getTotalProductSold()+dashboardProductStat.getTotalProductSold());

            } else {
                String name = categoryRepository.findById(dashboardProductStat.getIdCategory()).get().getName();
                DashboardProductStatDTO dashboardProductStatDTO = new DashboardProductStatDTO();
                dashboardProductStatDTO.setIdCategory(dashboardProductStat.getIdCategory());
                dashboardProductStatDTO.setName(name);
                dashboardProductStatDTO.setTotalProductSold(dashboardProductStat.getTotalProductSold());
                dashboardProductStatDTO.setTotalRevenue(dashboardProductStat.getTotalRevenue());
                dashboardStats.put(dashboardProductStat.getIdCategory(), dashboardProductStatDTO);
            }
        }

    }

    private List<DashboardProductStatDTO> createListDashboardProductStatDTOFromListDashboardProductDTO(List<GlobalStatAPA> globalStats) {
        Map<String,DashboardProductStatDTO> dashboardStats = new HashMap<>(globalStats.size());
        for (GlobalStatAPA globalStatAPA : globalStats) {
            createOrUploadKeyForDashboardStats(globalStatAPA,dashboardStats);
        }
        return new ArrayList<>(dashboardStats.values());
    }

    private GlobalProductStatDTO createGlobalProductStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GlobalProductStatDTO globalProductStatDTO = new GlobalProductStatDTO();
        globalProductStatDTO.setGeneralStat(sumProductGeneralStatsFromGlobalStatList(globalStats));
        globalProductStatDTO.setCustomCakeStat(sumCustomCakeStatFromCustomCakeStatList(globalStats));
        globalProductStatDTO.setDashboardProductStats(createListDashboardProductStatDTOFromListDashboardProductDTO(globalStats));
        return globalProductStatDTO;
    }


    private CustomCakeStat sumCustomCakeStatFromCustomCakeStatList(List<GlobalStatAPA> globalStats) {
        CustomCakeStat customCakeStat = new CustomCakeStat();
        List<StatLabel> labelAndValues = new ArrayList<>();

        // Inizializza i valori aggregati con i primi valori
        if (!globalStats.isEmpty()) {
            List<StatLabel> initialStats = globalStats.get(0).getProducts().getCustomCakeStat().getCustomCakeStats();
            for (StatLabel stat : initialStats) {
                // Crea una copia dei valori iniziali per l'aggregazione
                labelAndValues.add(new StatLabel(stat.getLabel(), stat.getTotal(), stat.getValue()));
            }
        }

        // Itera sugli altri GlobalStatAPA e somma i valori
        for (int i = 1; i < globalStats.size(); i++) {
            List<StatLabel> currentStats = globalStats.get(i).getProducts().getCustomCakeStat().getCustomCakeStats();

            // Somma i valori corrispondenti
            for (int j = 0; j < labelAndValues.size(); j++) {
                double currentValue = currentStats.get(j).getValue();
                long currentTotal = currentStats.get(j).getTotal();
                labelAndValues.get(j).setValue(labelAndValues.get(j).getValue() + currentValue);
                labelAndValues.get(j).setTotal(labelAndValues.get(j).getTotal() + currentTotal);
            }
        }

        customCakeStat.setCustomCakeStats(labelAndValues);
        return customCakeStat;
    }


    private GeneralProductStat sumProductGeneralStatsFromGlobalStatList(List<GlobalStatAPA> globalStats) {
        GeneralProductStat generalProductStat = new GeneralProductStat();
        for (GlobalStatAPA globalStat : globalStats) {
            generalProductStat.setTotalRevenue(generalProductStat.getTotalRevenue()+globalStat.getProducts().getGeneralStat().getTotalRevenue());
            generalProductStat.setTotalProductsSold(generalProductStat.getTotalProductsSold()+globalStat.getProducts().getGeneralStat().getTotalProductsSold());
            generalProductStat.setTotalRevenue(generalProductStat.getTotalOrders()+globalStat.getProducts().getGeneralStat().getTotalOrders());
            generalProductStat.setTotalRevenue(generalProductStat.getTotalCustomersServed()+globalStat.getProducts().getGeneralStat().getTotalCustomersServed());
        }
        return generalProductStat;
    }

    private GeneralIngredientStat sumIngredientGeneralStatsFromGlobalStatList(List<GlobalStatAPA> globalStats) {
        GeneralIngredientStat generalIngredientStat = new GeneralIngredientStat();
        for (GlobalStatAPA globalStat : globalStats) {
            generalIngredientStat.setTotalIngredients(generalIngredientStat.getTotalIngredients()+globalStat.getIngredients().getGeneralStat().getTotalIngredients());
            generalIngredientStat.setUsedIngredients(generalIngredientStat.getUsedIngredients()+globalStat.getIngredients().getGeneralStat().getUsedIngredients());
            generalIngredientStat.setTotalUsedIngredients(generalIngredientStat.getTotalUsedIngredients()+globalStat.getIngredients().getGeneralStat().getTotalUsedIngredients());
        }
        return generalIngredientStat;
    }
}


