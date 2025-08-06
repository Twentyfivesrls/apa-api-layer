package com.twentyfive.apaapilayer.mappers;

import com.twentyfive.apaapilayer.dtos.stats.*;
import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import com.twentyfive.apaapilayer.repositories.CompletedOrderRepository;
import com.twentyfive.apaapilayer.repositories.IngredientRepository;
import com.twentyfive.apaapilayer.repositories.TrayRepository;
import com.twentyfive.apaapilayer.services.IngredientService;
import com.twentyfive.apaapilayer.services.ProductFixedService;
import com.twentyfive.apaapilayer.services.ProductKgService;
import com.twentyfive.apaapilayer.services.TrayService;
import com.twentyfive.apaapilayer.utils.StringUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.StatLabel;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.stat.*;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GlobalStatMapperService {

    private final CompletedOrderRepository completedOrderRepository;
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;

    private final ProductKgService productKgService;
    private final ProductFixedService productFixedService;
    private final IngredientService ingredientService;
    private final IngredientMapperService ingredientMapperService;
    private final TrayRepository trayRepository;
    private final TrayService trayService;

    public GlobalStatAPA createGlobalStatByDate(LocalDate date) {
        GlobalStatAPA globalStat = new GlobalStatAPA();
        globalStat.setProducts(createGlobalProductStatByDate(date));
        globalStat.setIngredients(createGlobalIngredientStatByDate(date));
        globalStat.setTrays(createGlobalTrayStatByDate(date));
        return globalStat;
    }

    private GlobalTrayStat createGlobalTrayStatByDate(LocalDate date) {
        GlobalTrayStat globalTrayStat = new GlobalTrayStat();

        globalTrayStat.setGeneralTrayStat(createGeneralTrayStatByDate(date));
        globalTrayStat.setTrayStats(createTrayTypeStatByDate(date));
        globalTrayStat.setProductWeightedStats(createTrayProductWeightedStatByDate(date));
        globalTrayStat.setTrayMeasureStat(createTrayMeasureStatByDate(date));
        return globalTrayStat;
    }

    private TrayMeasureStat createTrayMeasureStatByDate(LocalDate date) {

        List<MeasureStat> measureStats = new ArrayList<>();

        List<String> labels = completedOrderRepository.findDistinctTrayMeasureByDate(date, OrderStatus.COMPLETO);

        for (String label : labels) {
            MeasureStat measureStat = new MeasureStat();
            measureStat.setLabel(label);
            measureStat.setQuantity(completedOrderRepository.countQuantityByTrayMeasureAndDate(label, date, OrderStatus.COMPLETO).orElse(0L));
            measureStats.add(measureStat);
        }
        return new TrayMeasureStat(measureStats);
    }

    private List<ProductWeightedStat> createTrayProductWeightedStatByDate(LocalDate date) {
        List<ProductWeightedStat> productWeightedStats = new ArrayList<>();

        List<String> productWeightedIds = completedOrderRepository.findDistinctTrayProductWeightedIdByDate(date, OrderStatus.COMPLETO);

        for (String productWeightedId : productWeightedIds) {
            ProductWeightedStat productWeightedStat = new ProductWeightedStat();

            productWeightedStat.setIdProduct(productWeightedId);
            productWeightedStat.setQuantity(completedOrderRepository.countTrayProductWeightedById(productWeightedId,date, OrderStatus.COMPLETO).orElse(0L));
            productWeightedStat.setTotalWeight(completedOrderRepository.countWeightTrayProductWeightedById(productWeightedId,date, OrderStatus.COMPLETO).orElse(0.0));

            productWeightedStats.add(productWeightedStat);
        }
        return productWeightedStats;
    }

    private List<TrayStat> createTrayTypeStatByDate(LocalDate date) {

        List<TrayStat> trayStats = new ArrayList<>();

        List<String> trayIds = completedOrderRepository.findDistinctTrayIdsByDate(date, OrderStatus.COMPLETO);

        for (String trayId : trayIds) {
            TrayStat trayStat = new TrayStat();
            trayStat.setId(trayId);
            trayStat.setQuantity(completedOrderRepository.countQuantityByTrayIdAndDate(trayId,date,OrderStatus.COMPLETO).orElse(0L));
            trayStat.setTotalRevenue(completedOrderRepository.countTotalRevenueByTrayIdAndDate(trayId,date,OrderStatus.COMPLETO).orElse(0.0));
            trayStat.setTotalWeight(completedOrderRepository.countTotalWeightByTrayIdAndDate(trayId,date,OrderStatus.COMPLETO).orElse(0.0));
            trayStats.add(trayStat);
        }

        return trayStats;
    }

    private GeneralTrayStat createGeneralTrayStatByDate(LocalDate date) {
        GeneralTrayStat generalTrayStat = new GeneralTrayStat();

        generalTrayStat.setQuantity(completedOrderRepository.countTraysByDate(date, OrderStatus.COMPLETO).orElse(0L));
        generalTrayStat.setTotalWeight(completedOrderRepository.countTrayWeightByDate(date, OrderStatus.COMPLETO).orElse(0.0));
        generalTrayStat.setTotalRevenue(completedOrderRepository.countTotalRevenueByDate(date, OrderStatus.COMPLETO).orElse(0.0));
        generalTrayStat.setTotalProductWeighted(completedOrderRepository.countTotalWeightedProductsByDate(date, OrderStatus.COMPLETO).orElse(0L));

        return generalTrayStat;
    }

    private GlobalIngredientStat createGlobalIngredientStatByDate(LocalDate date) {
        GlobalIngredientStat globalIngredientStat = new GlobalIngredientStat();
        globalIngredientStat.setGeneralStat(createGeneralIngredientStatByDate(date));
        globalIngredientStat.setCategoryStat(createGlobalCategoryStats(date));
        globalIngredientStat.setCategoryStats(createIngredientCategoryStat(date));
        return globalIngredientStat;
    }

    private List<CategoryIngredientStat> createIngredientCategoryStat(LocalDate date) {
        List<String> ingredientIds = completedOrderRepository.findDistinctIngredientIds(date, OrderStatus.COMPLETO);
        List<CategoryIngredientStat> categoryIngredientStats = new ArrayList<>();
        for (String ingredientId : ingredientIds) {
            CategoryIngredientStat categoryIngredientStat = new CategoryIngredientStat();
            categoryIngredientStat.setIdIngredient(ingredientId);
            categoryIngredientStat.setTotalTimeUsed(completedOrderRepository.countIngredientUsage(date, OrderStatus.COMPLETO, ingredientId).orElse(0L));
            categoryIngredientStats.add(categoryIngredientStat);
        }
        return categoryIngredientStats;
    }

    private List<GlobalCategoryStat> createGlobalCategoryStats(LocalDate date) {
        List<String> categoryIds = completedOrderRepository.findDistinctIngredientCategoryIds(date, OrderStatus.COMPLETO);
        List<GlobalCategoryStat> globalCategoryStats = new ArrayList<>();
        for (String categoryId : categoryIds) {
            GlobalCategoryStat globalCategoryStat = createGlobalCategoryStat(date, categoryId);
            globalCategoryStats.add(globalCategoryStat);
        }
        return globalCategoryStats;
    }

    private GlobalCategoryStat createGlobalCategoryStat(LocalDate date, String categoryId) {
        GlobalCategoryStat globalCategoryStat = new GlobalCategoryStat();
        globalCategoryStat.setIdCategory(categoryId);
        globalCategoryStat.setUsedIngredients(completedOrderRepository.countDistinctIngredientsByCategory(date, OrderStatus.COMPLETO, categoryId).orElse(0L));
        globalCategoryStat.setTotalUsedIngredients(completedOrderRepository.countIngredientsByCategory(date, OrderStatus.COMPLETO, categoryId).orElse(0L));
        return globalCategoryStat;
    }

    private GeneralIngredientStat createGeneralIngredientStatByDate(LocalDate date) {
        GeneralIngredientStat generalIngredientStat = new GeneralIngredientStat();
        generalIngredientStat.setTotalIngredients(ingredientRepository.count());
        generalIngredientStat.setDistinctUsedIngredients(completedOrderRepository.findUniqueIngredientIds(date, OrderStatus.COMPLETO));
        generalIngredientStat.setTotalUsedIngredients(completedOrderRepository.calculateTotalIngredients(date, OrderStatus.COMPLETO).orElse(0L));
        return generalIngredientStat;
    }

    private GlobalProductStat createGlobalProductStatByDate(LocalDate date) {
        List<String> categoryIds = completedOrderRepository.findDistinctCategoryIdsByDateAndStatus(date, OrderStatus.COMPLETO);
        GlobalProductStat globalProductStat = new GlobalProductStat();
        globalProductStat.setGeneralStat(createGeneralProductStatByDate(date));
        globalProductStat.setCustomCakeStat(createCustomStatByDate(date));
        globalProductStat.setDashboardProductStats(createDashboardProductStatsByDate(date, categoryIds));
        globalProductStat.setCategoryStats(createCategoryStatsByDate(date, categoryIds));
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
        categoryProductStat.setProductStats(createProductStatByDate(date, categoryId));
    }

    private List<ProductStatCategory> createProductStatByDate(LocalDate date, String categoryId) {
        List<ProductStatCategory> productStatCategories = new ArrayList<>();
        Set<String> productIdsByCategoryId = completedOrderRepository.findProductIdsByCategoryAndStatus(categoryId, OrderStatus.COMPLETO, date);
        for (String productId : productIdsByCategoryId) {
            ProductStatCategory productStatCategory = new ProductStatCategory();
            productStatCategory.setIdProduct(productId);
            productStatCategory.setQuantity(completedOrderRepository.sumQuantityByProductId(date, OrderStatus.COMPLETO, productId).orElse(0L));
            productStatCategory.setTotalRevenue(completedOrderRepository.sumTotalPriceByProductId(date, OrderStatus.COMPLETO, productId).orElse(0.0));
            productStatCategory.setTotalWeight(completedOrderRepository.sumTotalWeightByProductId(date, OrderStatus.COMPLETO, productId).orElse(0.0));
            productStatCategories.add(productStatCategory);
        }
        return productStatCategories;
    }

    private List<DashboardProductStat> createDashboardProductStatsByDate(LocalDate date, List<String> categoryIds) {
        List<DashboardProductStat> dashboardProductStats = new ArrayList<>();

        for (String categoryId : categoryIds) {
            DashboardProductStat dashboardProductStat = createDashboardProductStatByDate(date, categoryId);
            dashboardProductStats.add(dashboardProductStat);
        }

        return dashboardProductStats;


    }

    private DashboardProductStat createDashboardProductStatByDate(LocalDate date, String categoryId) {
        DashboardProductStat dashboardProductStat = new DashboardProductStat();
        dashboardProductStat.setIdCategory(categoryId);
        dashboardProductStat.setTotalRevenue(completedOrderRepository.sumTotalPriceByCategoryId(date, OrderStatus.COMPLETO, categoryId).orElse(0.0));
        dashboardProductStat.setTotalProductSold(completedOrderRepository.sumQuantityByCategoryId(date, OrderStatus.COMPLETO, categoryId).orElse(0L));
        return dashboardProductStat;
    }

    private CustomCakeStat createCustomStatByDate(LocalDate date) {
        CustomCakeStat customCakeStat = new CustomCakeStat(createStatLabels(date));
        return customCakeStat;
    }

    private List<StatLabel> createStatLabels(LocalDate date) {
        List<StatLabel> statLabels = new ArrayList<>();


        StatLabel personalizedCake = new StatLabel();
        personalizedCake.setLabel("Personalizzate");
        personalizedCake.setTotal(completedOrderRepository.sumQuantityByPickupDateStatusAndProductId(date, OrderStatus.COMPLETO, "6679566c03d8511e7a0d449c").orElse(0L));
        personalizedCake.setValue(completedOrderRepository.sumTotalPriceByPickupDateStatusAndProductId(date, OrderStatus.COMPLETO, "6679566c03d8511e7a0d449c").orElse(0.0));
        statLabels.add(personalizedCake);

        StatLabel trayStat = new StatLabel();
        trayStat.setLabel("Vassoi");
        trayStat.setTotal(completedOrderRepository.countTraysByDate(date, OrderStatus.COMPLETO).orElse(0L));
        trayStat.setValue(completedOrderRepository.countTotalPriceTraysByDate(date, OrderStatus.COMPLETO).orElse(0.0));
        statLabels.add(trayStat);

//        StatLabel planCakeStat = new StatLabel();
//        planCakeStat.setLabel("A Piani");
//        planCakeStat.setTotal(completedOrderRepository.sumQuantityByCategoryId(date, OrderStatus.COMPLETO, "670d2da65ab21a706dfa3c58").orElse(0L));
//        planCakeStat.setValue(completedOrderRepository.sumTotalPriceByCategoryId(date, OrderStatus.COMPLETO, "670d2da65ab21a706dfa3c58").orElse(0.0));
//        statLabels.add(planCakeStat);
//
//        StatLabel customCakeStat = new StatLabel();
//        customCakeStat.setLabel("Torte custom vendute");
//        customCakeStat.setTotal(personalizedCake.getTotal() + trayStat.getTotal() + planCakeStat.getTotal());
//        customCakeStat.setValue(personalizedCake.getValue() + trayStat.getValue() + planCakeStat.getValue());
//        statLabels.add(customCakeStat);

        return statLabels;
    }

    private GeneralProductStat createGeneralProductStatByDate(LocalDate date) {
        GeneralProductStat generalProductStat = new GeneralProductStat();
        generalProductStat.setTotalOrders(completedOrderRepository.countByPickupDateAndStatus(date, "COMPLETO"));
        generalProductStat.setTotalRevenue(completedOrderRepository.calculateTotalPriceByPickupDateAndStatus(date, OrderStatus.COMPLETO).orElse(0.0));
        generalProductStat.setDistinctCustomerServed(completedOrderRepository.findDistinctCustomerIdsByPickupDateAndStatus(date, OrderStatus.COMPLETO));
        generalProductStat.setTotalProductsSold(completedOrderRepository.sumQuantitiesByPickupDateAndStatus(date, OrderStatus.COMPLETO).orElse(0L));
        return generalProductStat;
    }

    public GlobalStatDTO createGlobalStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GlobalStatDTO globalStat = new GlobalStatDTO();
        globalStat.setGlobalProductStat(createGlobalProductStatDTOFromGlobalStat(globalStats));
        globalStat.setGlobalIngredientStat(createGlobalIngredientStatDTOFromGlobalIngredientStat(globalStats));
        return globalStat;
    }

    public GlobalTrayStatDTO createGlobalTrayStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GlobalTrayStatDTO globalStat = new GlobalTrayStatDTO();
        globalStat.setGeneralStat(createGeneralTrayStatDTOFromGlobalStat(globalStats));
        globalStat.setMeasureStats(createMeasureStatDTOFromGlobalStatList(globalStats));
        globalStat.setTrayStats(createGeneralGeneralTraySingleStatDTOFromGlobalStatList(globalStats));
        return globalStat;
    }

    private List<GeneralTraySingleStatDTO> createGeneralGeneralTraySingleStatDTOFromGlobalStatList(List<GlobalStatAPA> globalStats) {
        Map<String,GeneralTraySingleStatDTO> generalTraySingleStatDTOMap = new HashMap<>();
        for (GlobalStatAPA globalStatAPA : globalStats) {
            createOrUploadKeyForGeneralTraySingleStat(globalStatAPA, generalTraySingleStatDTOMap);
        }
        return new ArrayList<>(generalTraySingleStatDTOMap.values());
    }



    private List<GeneralMeasureStatDTO> createMeasureStatDTOFromGlobalStatList(List<GlobalStatAPA> globalStats) {
        Map<String, GeneralMeasureStatDTO> globalTrayStat = new HashMap<>(globalStats.size());
        for (GlobalStatAPA globalStatAPA : globalStats) {
            createOrUploadKeyForGlobalTrayStats(globalStatAPA, globalTrayStat);
        }
        return new ArrayList<>(globalTrayStat.values());

    }

    private GeneralTrayStatDTO createGeneralTrayStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GeneralTrayStatDTO generalStat = new GeneralTrayStatDTO();
        for (GlobalStatAPA globalStat : globalStats) {
            generalStat.setQuantity(generalStat.getQuantity() + globalStat.getTrays().getGeneralTrayStat().getQuantity());
            generalStat.setTotalProductWeighted(generalStat.getTotalProductWeighted() + globalStat.getTrays().getGeneralTrayStat().getTotalProductWeighted());
            generalStat.setTotalRevenue(generalStat.getTotalRevenue() + globalStat.getTrays().getGeneralTrayStat().getTotalRevenue());
            generalStat.setTotalWeight(generalStat.getTotalWeight() + globalStat.getTrays().getGeneralTrayStat().getTotalWeight());
        }
        return generalStat;

    }


    private GlobalProductStatDTO createGlobalProductStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        GlobalProductStatDTO globalProductStatDTO = new GlobalProductStatDTO();
        globalProductStatDTO.setGeneralStat(sumGlobalGeneralStatsFromGlobalStatList(globalStats));
        globalProductStatDTO.setCustomCakeStat(sumCustomCakeStatFromCustomCakeStatList(globalStats));
        globalProductStatDTO.setDashboardProductStats(createListDashboardProductStatDTOFromListDashboardProductDTO(globalStats));
        return globalProductStatDTO;
    }

    private GlobalIngredientStatDTO createGlobalIngredientStatDTOFromGlobalIngredientStat(List<GlobalStatAPA> globalStats) {
        GlobalIngredientStatDTO globalIngredientStatDTO = new GlobalIngredientStatDTO();
        globalIngredientStatDTO.setGeneralStat(sumIngredientGeneralStatsFromGlobalStatList(globalStats));
        globalIngredientStatDTO.setGlobalCategoryStats(createListGlobalCategoryStatDTOFromListDashboardProductDTO(globalStats));
        return globalIngredientStatDTO;
    }

    private List<GlobalCategoryStatDTO> createListGlobalCategoryStatDTOFromListDashboardProductDTO(List<GlobalStatAPA> globalStats) {
        Map<String, GlobalCategoryStatDTO> globalCategoryStats = new HashMap<>(globalStats.size());
        for (GlobalStatAPA globalStatAPA : globalStats) {
            createOrUploadKeyForGlobalCategoryStats(globalStatAPA, globalCategoryStats);
        }
        return new ArrayList<>(globalCategoryStats.values());
    }


    private List<DashboardProductStatDTO> createListDashboardProductStatDTOFromListDashboardProductDTO(List<GlobalStatAPA> globalStats) {
        Map<String, DashboardProductStatDTO> dashboardStats = new HashMap<>(globalStats.size());
        for (GlobalStatAPA globalStatAPA : globalStats) {
            createOrUploadKeyForDashboardStats(globalStatAPA, dashboardStats);
        }
        return new ArrayList<>(dashboardStats.values());
    }

    private void createOrUploadKeyForGlobalTrayStats(GlobalStatAPA globalStatAPA, Map<String, GeneralMeasureStatDTO> globalTrayStat) {
        for ( MeasureStat trayMeasureStat : globalStatAPA.getTrays().getTrayMeasureStat().getMeasureStats()) {
            String label = trayMeasureStat.getLabel();

            if (label == null || label.trim().isEmpty()) {
                // Puoi anche loggare un warning se vuoi:
                // log.warn("Trovata tray con label nulla o vuota, ignorata.");
                continue;
            }
            if (globalTrayStat.containsKey(trayMeasureStat.getLabel())) {
                GeneralMeasureStatDTO generalMeasureStatDTO = globalTrayStat.get(trayMeasureStat.getLabel());
                generalMeasureStatDTO.setPieces(generalMeasureStatDTO.getPieces() + trayMeasureStat.getQuantity());
            } else {
                GeneralMeasureStatDTO generalMeasureStatDTO = new GeneralMeasureStatDTO();
                generalMeasureStatDTO.setLabel(trayMeasureStat.getLabel());
                generalMeasureStatDTO.setPieces(trayMeasureStat.getQuantity());
                globalTrayStat.put(trayMeasureStat.getLabel(), generalMeasureStatDTO);
            }
        }

    }

    private void createOrUploadKeyForGeneralTraySingleStat(GlobalStatAPA globalStatAPA, Map<String, GeneralTraySingleStatDTO> generalTraySingleStatDTOMap) {
        for (TrayStat trayStat : globalStatAPA.getTrays().getTrayStats()) {
            if (generalTraySingleStatDTOMap.containsKey(trayStat.getId())){
                GeneralTraySingleStatDTO globalTraySingleStatDTO = generalTraySingleStatDTOMap.get(trayStat.getId());
                globalTraySingleStatDTO.setPrice(globalTraySingleStatDTO.getPrice() + trayStat.getTotalRevenue());
                globalTraySingleStatDTO.setQuantity(globalTraySingleStatDTO.getQuantity() + trayStat.getQuantity());
                globalTraySingleStatDTO.setWeight(globalTraySingleStatDTO.getWeight() + trayStat.getTotalWeight());
            } else {
                String name = trayRepository.findById(trayStat.getId()).get().getName();
                GeneralTraySingleStatDTO globalTraySingleStatDTO = new GeneralTraySingleStatDTO();
                globalTraySingleStatDTO.setId(trayStat.getId());
                globalTraySingleStatDTO.setName(name);
                globalTraySingleStatDTO.setPrice(trayStat.getTotalRevenue());
                globalTraySingleStatDTO.setQuantity(trayStat.getQuantity());
                globalTraySingleStatDTO.setWeight(trayStat.getTotalWeight());
                generalTraySingleStatDTOMap.put(trayStat.getId(), globalTraySingleStatDTO);

            }
        }
    }

    private void createOrUploadKeyForDashboardStats(GlobalStatAPA globalStatAPA, Map<String, DashboardProductStatDTO> dashboardStats) {
        for (DashboardProductStat dashboardProductStat : globalStatAPA.getProducts().getDashboardProductStats()) {
            if (dashboardStats.containsKey(dashboardProductStat.getIdCategory())) {
                DashboardProductStatDTO dashboardProductStatDTO = dashboardStats.get(dashboardProductStat.getIdCategory());
                dashboardProductStatDTO.setTotalRevenue(dashboardProductStatDTO.getTotalRevenue() + dashboardProductStat.getTotalRevenue());
                dashboardProductStatDTO.setTotalProductSold(dashboardProductStatDTO.getTotalProductSold() + dashboardProductStat.getTotalProductSold());

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

    private void createOrUploadKeyForGlobalCategoryStats(GlobalStatAPA globalStatAPA, Map<String, GlobalCategoryStatDTO> globalCategoryStats) {
        for (GlobalCategoryStat globalCategoryStat : globalStatAPA.getIngredients().getCategoryStat()) {
            if (globalCategoryStats.containsKey(globalCategoryStat.getIdCategory())) {
                GlobalCategoryStatDTO globalCategoryStatDTO = globalCategoryStats.get(globalCategoryStat.getIdCategory());
                globalCategoryStatDTO.setTotalUsedIngredients(globalCategoryStatDTO.getTotalUsedIngredients() + globalCategoryStat.getTotalUsedIngredients());
                globalCategoryStatDTO.setUsedIngredients(globalCategoryStatDTO.getUsedIngredients() + globalCategoryStat.getUsedIngredients());
            } else if (globalCategoryStat.getIdCategory() != null) {
                String name = categoryRepository.findById(globalCategoryStat.getIdCategory()).get().getName();
                GlobalCategoryStatDTO globalCategoryStatDTO = new GlobalCategoryStatDTO();
                globalCategoryStatDTO.setIdCategory(globalCategoryStat.getIdCategory());
                globalCategoryStatDTO.setName(name);
                globalCategoryStatDTO.setTotalUsedIngredients(globalCategoryStat.getTotalUsedIngredients());
                globalCategoryStatDTO.setUsedIngredients(globalCategoryStat.getUsedIngredients());
                globalCategoryStats.put(globalCategoryStat.getIdCategory(), globalCategoryStatDTO);
            }
        }

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


    private GeneralProductStatDTO sumGlobalGeneralStatsFromGlobalStatList(List<GlobalStatAPA> globalStats) {
        GeneralProductStatDTO generalProductStat = new GeneralProductStatDTO();
        for (GlobalStatAPA globalStat : globalStats) {
            generalProductStat.setTotalRevenue(generalProductStat.getTotalRevenue() + globalStat.getProducts().getGeneralStat().getTotalRevenue());
            generalProductStat.setTotalProductsSold(generalProductStat.getTotalProductsSold() + globalStat.getProducts().getGeneralStat().getTotalProductsSold());
            generalProductStat.setTotalOrders(generalProductStat.getTotalOrders() + globalStat.getProducts().getGeneralStat().getTotalOrders());
            generalProductStat.setDistinctCustomerServed(StringUtilities.mergeUniqueValues(generalProductStat.getDistinctCustomerServed(), globalStat.getProducts().getGeneralStat().getDistinctCustomerServed()));
        }
        return generalProductStat;
    }

    private GeneralIngredientStatDTO sumIngredientGeneralStatsFromGlobalStatList(List<GlobalStatAPA> globalStats) {
        GeneralIngredientStatDTO generalIngredientStat = new GeneralIngredientStatDTO();

        for (GlobalStatAPA globalStat : globalStats) {

            generalIngredientStat.setTotalIngredients(
                    Math.max(
                            generalIngredientStat.getTotalIngredients(),
                            globalStat.getIngredients().getGeneralStat().getTotalIngredients()
                    ));

            generalIngredientStat.setTotalUsedIngredients(generalIngredientStat.getTotalUsedIngredients() + globalStat.getIngredients().getGeneralStat().getTotalUsedIngredients());
            generalIngredientStat.setDistinctUsedIngredients(StringUtilities.mergeUniqueValues(generalIngredientStat.getDistinctUsedIngredients(), globalStat.getIngredients().getGeneralStat().getDistinctUsedIngredients()));
        }
        return generalIngredientStat;
    }

    public List<ProductStatCategoryDTO> getProductStatCategoriesDTOFromGlobalStats(List<GlobalStatAPA> globalStats, CategoryAPA category) {
        String categoryId = category.getId();
        Map<String, ProductStatCategoryDTO> productStatMap = new HashMap<>();

        for (GlobalStatAPA globalStat : globalStats) {

            CategoryProductStat categoryProductStat = globalStat.getProducts().getCategoryStats().stream()
                    .filter(stat -> categoryId.equals(stat.getIdCategory()))
                    .findFirst()
                    .orElse(null);

            if (categoryProductStat != null) {
                for (ProductStatCategory productStat : categoryProductStat.getProductStats()) {
                    productStatMap.merge(
                            productStat.getIdProduct(),
                            new ProductStatCategoryDTO(
                                    productStat.getIdProduct(),
                                    getProductName(category.getType(), productStat.getIdProduct()),
                                    getIngredientNames(category.getType(),productStat.getIdProduct()),
                                    productStat.getQuantity(),
                                    productStat.getTotalWeight(),
                                    productStat.getTotalRevenue()
                            ),
                            (existing, newEntry) -> {
                                existing.setQuantity(existing.getQuantity() + newEntry.getQuantity());
                                existing.setTotalWeight(existing.getTotalWeight() + newEntry.getTotalWeight());
                                existing.setTotalRevenue(existing.getTotalRevenue() + newEntry.getTotalRevenue());
                                return existing;
                            }
                    );
                }
            }
        }

        return new ArrayList<>(productStatMap.values());
    }

    private String getIngredientNames(String type, String idProduct) {
        List<String> ingredients = switch (type) {
            case "productFixed" -> productFixedService.getById(idProduct).getIngredients();
            case "productKg","productWeighted" -> productKgService.getById(idProduct).getIngredients();
            default -> Collections.emptyList();
        };

        return String.join(", ", ingredients);
    }

    private String getProductName(String type, String idProduct) {
        switch (type) {
            case "productFixed" -> {
                return productFixedService.getById(idProduct).getName();
            }
            case "productKg" -> {
                return productKgService.getById(idProduct).getName();
            }
            case "tray" -> {
                return trayService.getById(idProduct).getName();
            }
            default -> {
                return "No Matching Product Name";
            }
        }
    }

    public List<ProductWeightedStatCategoryDTO> getProductWeightedStatCategoryDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
// Mappa per raggruppare gli ingredienti per idIngredient
        Map<String, ProductWeightedStatCategoryDTO> productWeightedStatMap = new HashMap<>();

        // Cicla su ogni globalStat e sui suoi ingredienti
        for (GlobalStatAPA globalStat : globalStats) {
            if(globalStat.getTrays().getProductWeightedStats() != null && globalStat.getTrays().getProductWeightedStats().size() > 0) {
                for (ProductWeightedStat productWeightedStat : globalStat.getTrays().getProductWeightedStats()) {
                    if (productWeightedStat.getIdProduct() != null) {
                        String productWeightedId = productWeightedStat.getIdProduct();
                        long quantity = productWeightedStat.getQuantity();
                        double weight = productWeightedStat.getTotalWeight();

                        // Se l'productWeightedId è già presente nella mappa, somma il quantity
                        productWeightedStatMap.merge(productWeightedId,
                                new ProductWeightedStatCategoryDTO(productWeightedId, getProductName(productWeightedId), getIngredientNames("productWeighted", productWeightedId), quantity, weight),
                                (existing, newEntry) -> {
                                    existing.setQuantity(existing.getQuantity() + newEntry.getQuantity());
                                    existing.setTotalWeight(existing.getTotalWeight() + newEntry.getTotalWeight());
                                    return existing;
                                });
                    }
                }
            }
        }

        // Restituisci la lista dei DTO
        return new ArrayList<>(productWeightedStatMap.values());    }

    public List<IngredientStatDTO> getListIngredientStatDTOFromGlobalStat(List<GlobalStatAPA> globalStats) {
        // Mappa per raggruppare gli ingredienti per idIngredient
        Map<String, IngredientStatDTO> ingredientStatMap = new HashMap<>();

        // Cicla su ogni globalStat e sui suoi ingredienti
        for (GlobalStatAPA globalStat : globalStats) {
            for (CategoryIngredientStat categoryIngredientStat : globalStat.getIngredients().getCategoryStats()) {
                String ingredientId = categoryIngredientStat.getIdIngredient();
                long totalTimeUsed = categoryIngredientStat.getTotalTimeUsed();

                // Se l'ingredientId è già presente nella mappa, somma il totalTimeUsed
                ingredientStatMap.merge(ingredientId,
                        new IngredientStatDTO(ingredientId, getIngredientName(ingredientId),totalTimeUsed),
                        (existing, newEntry) -> {
                            existing.setTotalTimeUsed(existing.getTotalTimeUsed() + newEntry.getTotalTimeUsed());
                            return existing;
                        });
            }
        }

        // Restituisci la lista dei DTO
        return new ArrayList<>(ingredientStatMap.values());
    }

    private String getIngredientName(String ingredientId) {
        if (ingredientId == null) {
            return "no ingredient found";
        }
        return ingredientService.getById(ingredientId).getName();
    }
    private String getProductName(String productId) {
        if (productId == null) {
            return "no product found";
        }
        return productFixedService.getById(productId).getName();
    }


    public OrderStatDTO createOrderStatFromGlobalStat(List<GlobalStatAPA> globalStats, long days) {
        OrderStatDTO orderStat = new OrderStatDTO();

        long totalOrders= countTotalOrders(globalStats);
        double avgPrice = countTotalPrice(globalStats)/totalOrders;
        double avgOrders = (double) totalOrders/days;
        double avgCustomer = (double) totalOrders/countTotalCustomer(globalStats);

        BigDecimal avgPriceRounded = toSafeBigDecimal(avgPrice);
        BigDecimal avgOrdersRounded = toSafeBigDecimal(avgOrders);
        BigDecimal avgCustomerRounded = toSafeBigDecimal(avgCustomer);


        orderStat.setTotalOrders(totalOrders);
        orderStat.setAvgCustomer(avgCustomerRounded.doubleValue());
        orderStat.setAvgOrders(avgOrdersRounded.doubleValue());
        orderStat.setAvgPrice(avgPriceRounded.doubleValue());

        return orderStat;
    }

    private double countTotalPrice(List<GlobalStatAPA> globalStats) {
        long total = 0;

        for (GlobalStatAPA globalStat : globalStats) {
            total +=globalStat.getProducts().getGeneralStat().getTotalRevenue();
        }

        return total;
    }

    private long countTotalCustomer(List<GlobalStatAPA> globalStats) {
        long totalCustomer = 0;

        for (GlobalStatAPA globalStat : globalStats) {
            totalCustomer += globalStat.getProducts().getGeneralStat().getDistinctCustomerServed().size(); //FIXME non solo quelli distinti
        }

        return totalCustomer > 0 ? totalCustomer : 1;

    }

    private long countTotalOrders(List<GlobalStatAPA> globalStats) {
        long totalOrders = 0;

        for (GlobalStatAPA globalStat : globalStats) {
            totalOrders+=globalStat.getProducts().getGeneralStat().getTotalOrders();
        }

        return totalOrders;
    }

    private BigDecimal toSafeBigDecimal(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

}


