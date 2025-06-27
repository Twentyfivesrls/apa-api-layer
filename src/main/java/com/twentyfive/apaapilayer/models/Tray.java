package com.twentyfive.apaapilayer.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Bundle;

@Document("trays")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tray extends Bundle {
    private boolean toPrepare = true; //Il prodotto se è da preparare o meno
    @DBRef
    private ProductStatAPA stats;
}
