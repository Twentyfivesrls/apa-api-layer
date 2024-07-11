package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.MenuSectionAPA;
import com.twentyfive.apaapilayer.models.ProductKgAPA;
import com.twentyfive.apaapilayer.models.ProductWeightedAPA;
import com.twentyfive.apaapilayer.repositories.MenuSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuSectionService {
    private final MenuSectionRepository menuSectionRepository;
    private final MongoTemplate mongoTemplate;

    public List<MenuSectionAPA> getAll(){
        return menuSectionRepository.findAll();
    }
    public MenuSectionAPA getById(String id) {
        Optional<MenuSectionAPA> optMenuSection = menuSectionRepository.findById(id);
        if(optMenuSection.isPresent()){
            MenuSectionAPA menuSection = optMenuSection.get();
            return menuSection;
        }
        throw new NoSuchElementException("Non esiste una sezione con questo id!");
    }

    public MenuSectionAPA updateById(String id,MenuSectionAPA menuSectionAPA) {
        Optional<MenuSectionAPA> optMenuSection = menuSectionRepository.findById(id);
        if(optMenuSection.isPresent()){
            MenuSectionAPA menuSectionToPatch = optMenuSection.get();
            BeanUtils.copyProperties(menuSectionAPA,menuSectionToPatch,"id");
            return menuSectionRepository.save(menuSectionToPatch);
        }
        throw new NoSuchElementException("Non esiste una sezione con questo id!");
    }

    public MenuSectionAPA save(MenuSectionAPA menuSectionAPA) {
        return menuSectionRepository.save(menuSectionAPA);
    }

    public Boolean deleteById(String id) {
        Optional<MenuSectionAPA> optMenuSection = menuSectionRepository.findById(id);
        if(optMenuSection.isPresent()) {
            menuSectionRepository.deleteById(id);
            Query query = new Query(Criteria.where("idSection").in(id));
            mongoTemplate.remove(query, CategoryAPA.class);
            return true;
        }
        return false;
    }
}
