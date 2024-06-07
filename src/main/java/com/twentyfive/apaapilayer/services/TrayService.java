package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.TrayAPADTO;
import com.twentyfive.apaapilayer.dtos.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.models.Tray;
import com.twentyfive.apaapilayer.repositories.TrayRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.apaapilayer.utils.TrayUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrayService {

    private final TrayRepository trayRepository;

    public Page<TrayAPADTO> findByIdCategory(String idCategory,int page, int size, String sortColumn, String sortDirection) {
        List<Tray> trays = trayRepository.findAllByCategoryId(idCategory);
        List<TrayAPADTO> realTrays = new ArrayList<>();
        for(Tray tray : trays){
            realTrays.add(TrayUtilities.mapToTrayAPADTO(tray));
        }
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(realTrays,pageable);
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"name");
        Pageable pageable=PageRequest.of(page,size,sort);
        return PageUtilities.convertListToPageWithSorting(realTrays,pageable);
    }
    public TrayDetailsAPADTO getById(String id) {
        Tray tray=trayRepository.findById(id).orElse(null);
        if(tray != null){
            return TrayUtilities.mapToTrayDetailsAPADTO(tray);
        }
        return null;
    }

    public Tray save(Tray tray) {
        return trayRepository.save(tray);
    }

    public Boolean activateOrDisableById(String id) {
        Tray tray =trayRepository.findById(id).orElse(null);
        if(tray!=null){
            tray.setActive(!(tray.isActive()));
            trayRepository.save(tray);
            return true;
        }
        return false;
    }

    public Page<TrayAPADTO> getAllActive(String idCategory,int page, int size) {
        List<Tray> trays = trayRepository.findAllByCategoryIdAndCustomizedFalseAndActiveTrueOrderByNameAsc(idCategory);
        List<TrayAPADTO> realTrays = new ArrayList<>();
        for(Tray tray : trays){
            realTrays.add(TrayUtilities.mapToTrayAPADTO(tray));
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realTrays,pageable);
    }

    public String getImageUrl(String id) {
        Tray tray=trayRepository.findById(id).orElse(null);
        return tray.getImageUrl();

    }
}
