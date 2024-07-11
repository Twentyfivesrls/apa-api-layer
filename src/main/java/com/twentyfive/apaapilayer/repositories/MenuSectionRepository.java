package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.MenuItemAPA;
import com.twentyfive.apaapilayer.models.MenuSectionAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuSectionRepository extends MongoRepository<MenuSectionAPA,String> {
}
