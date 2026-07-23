package com.twentyfive.apaapilayer.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.TimeSlot;

@Document("timeslots")
@Getter
@Setter
public class TimeSlotAPA extends TimeSlot {

    // Categoria a cui appartiene questo insieme di slot (capacità per categoria).
    // null = documento legacy globale (retrocompatibilità).
    private String categoryId;

}
