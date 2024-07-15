package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.dtos.MenuItemDTO;
import com.twentyfive.apaapilayer.models.MenuItemAPA;
import com.twentyfive.apaapilayer.repositories.AllergenRepository;
import com.twentyfive.apaapilayer.repositories.MenuItemRepository;
import com.twentyfive.apaapilayer.utils.PageUtilities;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.utils.Allergen;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final AllergenRepository allergenRepository;

    public List<MenuItemDTO> getAll() {
        List<MenuItemAPA> menuItems = menuItemRepository.findAll();
        List<MenuItemDTO> menuItemsDTO = new ArrayList<>();
        for (MenuItemAPA menuItem : menuItems) {
            List<Allergen> allergens = getAllergensFromMenuItem(menuItem);
            MenuItemDTO menuItemDTO = new MenuItemDTO(menuItem,allergens);
            menuItemsDTO.add(menuItemDTO);
        }
        return menuItemsDTO;
    }

    public MenuItemDTO getById(String id) {
        Optional<MenuItemAPA> optMenuItem = menuItemRepository.findById(id);
        if (optMenuItem.isPresent()){
            MenuItemAPA menuItem = optMenuItem.get();
            List<Allergen> allergens = getAllergensFromMenuItem(menuItem);
            return new MenuItemDTO(menuItem,allergens);
        }
        throw new NoSuchElementException("Nessun prodotto con questo id!");
    }

    public List<MenuItemDTO> getAllByIdCategoryAndActiveTrue(String id) {
        List<MenuItemAPA> menuItems = menuItemRepository.findAllByCategoryIdAndActiveTrue(id);
        List<MenuItemDTO> menuItemsDTO = mapListMenuItemsToMenuItemDTO(menuItems);
        return menuItemsDTO;
    }
    public Page<MenuItemDTO> getAllByIdCategoryPaginated(String idCategory, int page, int size, String sortColumn, String sortDirection) {
        List<MenuItemAPA> menuItems = menuItemRepository.findAllByCategoryId(idCategory);
        List<MenuItemDTO> menuItemsDTO = mapListMenuItemsToMenuItemDTO(menuItems);
        if(!(sortDirection.isBlank() || sortColumn.isBlank())){
            if(sortColumn.equals("price")){
                sortColumn = "realPrice";
            }
            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortColumn);
            Pageable pageable= PageRequest.of(page,size,sort);
            return PageUtilities.convertListToPageWithSorting(menuItemsDTO,pageable);
        }
        Sort sort = Sort.by(Sort.Direction.ASC,"name");
        Pageable pageable=PageRequest.of(page,size,sort);
        return PageUtilities.convertListToPageWithSorting(menuItemsDTO,pageable);

    }

    public MenuItemAPA save(MenuItemAPA menuItemAPA) {
        return menuItemRepository.save(menuItemAPA);
    }

    public MenuItemAPA updateById(String id, MenuItemAPA menuItemAPA) {
        Optional<MenuItemAPA> optMenuItem = menuItemRepository.findById(id);
        if(optMenuItem.isPresent()){
            MenuItemAPA menuItemToPatch = optMenuItem.get();
            BeanUtils.copyProperties(menuItemAPA,menuItemToPatch,"id");
            return menuItemRepository.save(menuItemToPatch);

        }
        throw new NoSuchElementException("Nessun prodotto con questo id!");
    }
    public boolean deleteById(String id) {
        Optional<MenuItemAPA> optMenuItem = menuItemRepository.findById(id);
        if(optMenuItem.isPresent()){
            menuItemRepository.deleteById(id);
            return true;
        }
        throw new NoSuchElementException("Nessun prodotto con questo id");
    }

    public boolean activateOrDisableById(String id) {
        Optional<MenuItemAPA> optMenuItem = menuItemRepository.findById(id);
        if(optMenuItem.isPresent()){
            MenuItemAPA menuItemAPA = optMenuItem.get();
            menuItemAPA.setActive(!menuItemAPA.isActive());
            menuItemRepository.save(menuItemAPA);
            return true;
        }
        throw new NoSuchElementException("Nessun prodotto con questo id");
    }


    private List<Allergen> getAllergensFromMenuItem(MenuItemAPA menuItem){
        List<Allergen> allergens = new ArrayList<>();
        for (String allergenName : menuItem.getAllergenNames()) {
            Optional<Allergen> optAllergen = allergenRepository.findByName(allergenName);
            if(optAllergen.isPresent()){
                Allergen allergen = optAllergen.get();
                allergens.add(allergen);
            }
        }
        return allergens;
    }
    private List<MenuItemDTO> mapListMenuItemsToMenuItemDTO(List<MenuItemAPA> menuItems){
        List<MenuItemDTO> menuItemsDTO = new ArrayList<>();
        for (MenuItemAPA menuItem : menuItems) {
            List<Allergen> allergens = getAllergensFromMenuItem(menuItem);
            MenuItemDTO menuItemDTO = new MenuItemDTO(menuItem,allergens);
            menuItemsDTO.add(menuItemDTO);
        }
        return menuItemsDTO;
    }
}
