package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.*;
import com.twentyfive.apaapilayer.repositories.CategoryRepository;
import com.twentyfive.apaapilayer.repositories.ProductStatRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import twentyfive.twentyfiveadapter.generic.ecommerce.models.persistent.Product;

import java.util.Optional;

@Data
@RequiredArgsConstructor
@Service
public class ProductStatService {
    private final ProductStatRepository productStatRepository;
    private final CategoryRepository categoryRepository;

    public void addBuyingCountProduct(Product product, int n) {
        Optional<ProductStatAPA> productStat;
        if (product instanceof ProductWeightedAPA){
            ProductWeightedAPA productWeightedAPA=(ProductWeightedAPA) product;
            productStat = productStatRepository.findById(productWeightedAPA.getStats().getId());
        } else if (product instanceof  ProductKgAPA){
            ProductKgAPA productKgAPA=(ProductKgAPA) product;
            productStat = productStatRepository.findById(productKgAPA.getStats().getId());
        } else {
            ProductFixedAPA productFixedAPA=(ProductFixedAPA) product;
            productStat = productStatRepository.findById(productFixedAPA.getStats().getId());
        }
        if (productStat.isPresent()){
            productStat.get().setBuyingCount(productStat.get().getBuyingCount()+n);
            productStatRepository.save(productStat.get());
        } else {
            Optional<CategoryAPA> category = categoryRepository.findById(product.getCategoryId());
            if (category.isPresent()){
                ProductStatAPA newProductStat = new ProductStatAPA(category.get().getType());
                newProductStat.setBuyingCount(n);
                productStatRepository.save(newProductStat);
            }
        }
    }

    public void addBuyingCountTray(Tray tray, int n) {
        Optional<ProductStatAPA> productStat = productStatRepository.findById(tray.getStats().getId());
        if (productStat.isPresent()){
            productStat.get().setBuyingCount(productStat.get().getBuyingCount()+n);
            productStatRepository.save(productStat.get());
        } else {
            Optional<CategoryAPA> category = categoryRepository.findById(tray.getCategoryId());
            if (category.isPresent()){
                ProductStatAPA newProductStat = new ProductStatAPA(category.get().getType());
                newProductStat.setBuyingCount(n);
                productStatRepository.save(newProductStat);
            }
        }
    }
}
