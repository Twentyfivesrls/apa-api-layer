package com.twentyfive.apaapilayer.dtos.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTrayStatDTO {
    private GeneralTrayStatDTO generalStat;
    private List<GeneralMeasureStatDTO> measureStats;
    private List<GeneralTraySingleStatDTO> trayStats;
}
