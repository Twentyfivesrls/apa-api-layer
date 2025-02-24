package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.stats.GlobalStatDTO;
import com.twentyfive.apaapilayer.dtos.stats.ProductStatCategoryDTO;
import com.twentyfive.apaapilayer.exceptions.GlobalStatNotFoundException;
import com.twentyfive.apaapilayer.mappers.GlobalStatMapperService;
import com.twentyfive.apaapilayer.models.DateRange;
import com.twentyfive.apaapilayer.models.GlobalStatAPA;
import com.twentyfive.apaapilayer.repositories.GlobalStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.GlobalStat;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class GlobalStatService {

    private final GlobalStatMapperService globalStatMapperService;
    private final GlobalStatRepository globalStatRepository;

    public Boolean add(GlobalStatAPA globalStat) {
        if(globalStat.getId() == null) {
            globalStat.setId(LocalDate.now());
        }
        globalStatRepository.save(globalStat);
        return true;
    }

    public GlobalStat getById(LocalDate id) {
        return globalStatRepository.findById(id).orElseThrow(() -> new GlobalStatNotFoundException("globalStat not found at this " +id));
    }

    public Boolean createByDate(LocalDate date) {
        GlobalStatAPA globalStat = globalStatMapperService.createGlobalStatByDate(date);
        globalStat.setId(date);
        globalStatRepository.save(globalStat);
        return true;
    }

    public GlobalStatDTO getGlobalFilteredStats(DateRange date) {
        if(LocalDate.now().isAfter(date.getStartDate()) || LocalDate.now().isBefore(date.getEndDate())) {
            createByDate(LocalDate.now());
        }
        List<GlobalStatAPA> globalStats = globalStatRepository.findByIdBetweenInclusive(date.getStartDate(), date.getEndDate());
        return globalStatMapperService.createGlobalStatDTOFromGlobalStat(globalStats);
    }

    public Page<ProductStatCategoryDTO> getProductStatCategory(DateRange date, int page, int size, String sortColumn, String sortDirection,String value) {
        return null;
    }
}
