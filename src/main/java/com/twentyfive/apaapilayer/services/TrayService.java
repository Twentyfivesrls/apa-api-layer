package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.TrayAPADTO;
import com.twentyfive.apaapilayer.dtos.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import com.twentyfive.apaapilayer.models.Tray;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.ProductStatRepository;
import com.twentyfive.apaapilayer.repositories.TrayRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.apaapilayer.utils.TrayUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrayService {

    private final TrayRepository trayRepository;
    private final AllergenRepository allergenRepository;
    private final ProductStatRepository productStatRepository;

    public Page<TrayAPADTO> findByIdCategory(String idCategory,int page, int size, String sortColumn, String sortDirection) {
        List<Tray> trays = trayRepository.findAllByCategoryIdAndSoftDeletedFalse(idCategory);
        List<TrayAPADTO> realTrays = new ArrayList<>();
        for(Tray tray : trays){
            List<Allergen> realAllergens = new ArrayList<>();
            if(tray.getAllergenNames()!=null){
                realAllergens=allergensTray(tray.getAllergenNames());
            }
            realTrays.add(TrayUtilities.mapToTrayAPADTO(tray,realAllergens));
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
            List<Allergen> realAllergens = new ArrayList<>();
            if(tray.getAllergenNames()!=null){
                realAllergens = allergensTray(tray.getAllergenNames());
            }
            return TrayUtilities.mapToTrayDetailsAPADTO(tray, realAllergens);

        }
        return null;
    }

    public Tray save(Tray tray) {
        if(tray.getId()==null){
            ProductStatAPA pStat=new ProductStatAPA("tray");
            tray.setStats(pStat);
            productStatRepository.save(pStat);
        }
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
        List<Tray> trays = trayRepository.findAllByCategoryIdAndCustomizedFalseAndActiveTrueAndSoftDeletedFalseOrderByNameAsc(idCategory);
        List<TrayAPADTO> realTrays = new ArrayList<>();
        for(Tray tray : trays){
            List<Allergen> realAllergens = new ArrayList<>();
            if(tray.getAllergenNames()!=null){
                realAllergens=allergensTray(tray.getAllergenNames());
            }
            realTrays.add(TrayUtilities.mapToTrayAPADTO(tray,realAllergens));
        }
        Pageable pageable=PageRequest.of(page,size);
        return PageUtilities.convertListToPage(realTrays,pageable);
    }

    public String getImageUrl(String id) {
        Tray tray=trayRepository.findById(id).orElse(null);
        return tray.getImageUrl();

    }

    public boolean deleteById(String id) {
        Optional<Tray> optTray = trayRepository.findById(id);
        if (optTray.isPresent()){
            Tray tray = optTray.get();
            tray.setSoftDeleted(true);
            trayRepository.save(tray);
            return true;
        }
        return false;
    }

    private List<Allergen> allergensTray(List<String> allergenNames){
        List<Allergen> realAllergeni = new ArrayList<>();
        for(String allergene : allergenNames){
            Allergen realAllergene = allergenRepository.findByName(allergene).orElse(null);
            if(realAllergene!=null)
                realAllergeni.add(realAllergene);
        }
        return realAllergeni;
    }
}
