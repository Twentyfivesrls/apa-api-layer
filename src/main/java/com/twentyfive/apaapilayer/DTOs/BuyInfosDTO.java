package com.twentyfive.apaapilayer.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.james.mime4j.dom.datetime.DateTime;

import java.time.LocalDateTime;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuyInfosDTO {
    private List<Integer> positions;
    private LocalDateTime selectedPickupDateTime;
    private String note;

}
