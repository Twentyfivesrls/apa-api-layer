package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.stat.CustomCakeStat;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.dinamic.stat.GeneralProductStat;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalProductStatDTO {
    private GeneralProductStat generalStat;

    private CustomCakeStat customCakeStat;

    private List<DashboardProductStatDTO> dashboardProductStats;


}
