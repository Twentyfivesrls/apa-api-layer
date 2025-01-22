package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.GlobalStat;

import java.time.LocalDate;

@Document(value = "globalStats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalStatAPA extends GlobalStat {
    @Id
    private LocalDate id; // Puoi usare LocalDate se ti basta una precisione giornaliera
}
