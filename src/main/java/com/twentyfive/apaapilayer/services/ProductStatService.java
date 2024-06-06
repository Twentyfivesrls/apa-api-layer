package com.twentyfive.apaapilayer.services;

import com.twentyfive.apaapilayer.models.CategoryAPA;
import com.twentyfive.apaapilayer.models.ProductStatAPA;
import com.twentyfive.apaapilayer.models.Tray;
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
        Optional<ProductStatAPA> productStat = productStatRepository.findById(product.getId());
        if (productStat.isPresent()){
            productStat.get().setBuyingCount(productStat.get().getBuyingCount()+n);
            productStatRepository.save(productStat.get());
        } else {
            Optional<CategoryAPA> category = categoryRepository.findById(product.getCategoryId());
            if (category.isPresent()){
                ProductStatAPA newProductStat = new ProductStatAPA();
                newProductStat.setId(product.getId());
                newProductStat.setType(category.get().getType());
                newProductStat.setBuyingCount(n);
                productStatRepository.save(newProductStat);
            }
        }
    }

    public void addBuyingCountTray(Tray tray, int n) {
        Optional<ProductStatAPA> productStat = productStatRepository.findById(tray.getId());
        if (productStat.isPresent()){
            productStat.get().setBuyingCount(productStat.get().getBuyingCount()+n);
            productStatRepository.save(productStat.get());
        } else {
            Optional<CategoryAPA> category = categoryRepository.findById(tray.getCategoryId());
            if (category.isPresent()){
                ProductStatAPA newProductStat = new ProductStatAPA();
                newProductStat.setId(tray.getId());
                newProductStat.setType(category.get().getType());
                newProductStat.setBuyingCount(n);
                productStatRepository.save(newProductStat);
            }
        }
    }
}
