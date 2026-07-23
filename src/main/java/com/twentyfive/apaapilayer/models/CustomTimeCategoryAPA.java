package com.twentyfive.apaapilayer.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.CustomTimeCategory;

import java.time.LocalTime;

@Document("customTimeCategories")
@Data
@NoArgsConstructor
public class CustomTimeCategoryAPA extends CustomTimeCategory {
    @DBRef
    @Indexed(unique = true)
    private CategoryAPA category;

    // null = categoria non ancora configurata con la nuova logica → fallback vecchio comportamento
    private Integer daysAhead;
    private LocalTime cutoffHour;
    private LocalTime cutoffResetHour;
    private LocalTime firstPickupAfterCutoff;
    private boolean sameDayAllowed = false;

    // Capacità massima ordini (da preparare) per ora, specifica della categoria.
    // null = usa il default globale di Setting (maxMorningOrder / maxAfternoonOrder)
    private Integer maxMorningOrder;
    private Integer maxAfternoonOrder;

    // null = nessuna variante configurata; non-null = regola alternativa per prodotti personalizzati/grandi
    private CustomTimeVariantAPA variant;
}
