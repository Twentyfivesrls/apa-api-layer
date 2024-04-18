package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.DTOs.TrayAPADTO;
import com.twentyfive.apaapilayer.DTOs.TrayDetailsAPADTO;
import com.twentyfive.apaapilayer.models.Tray;
import com.twentyfive.apaapilayer.repositories.TrayRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import com.twentyfive.apaapilayer.utils.TrayUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrayService {

    private final TrayRepository trayRepository;

    public Page<TrayAPADTO> getAll(int page, int size) {
        Pageable pageable= PageRequest.of(page,size);
        List<Tray> trays = trayRepository.findAll();
        List<TrayAPADTO> realTrays = new ArrayList<>();
        for(Tray tray : trays){
            realTrays.add(TrayUtilities.mapToTrayAPADTO(tray));
        }
        return PageUtilities.convertListToPage(realTrays,pageable);
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
            tray.setEnabled(!(tray.isEnabled()));
            return true;
        }
        return false;
    }
}
