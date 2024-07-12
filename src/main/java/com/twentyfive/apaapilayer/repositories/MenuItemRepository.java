package com.twentyfive.apaapilayer.repositories;

import com.twentyfive.apaapilayer.models.MenuItemAPA;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends MongoRepository<MenuItemAPA,String> {

    List<MenuItemAPA> findAllByCategoryId(String categoryId);

}
