package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.repository.ProductRepository;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    public Product save(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }
        return productRepository.save(product);
    }

    public Product update(Long id, Product updatedProduct) {
        Product target = findById(id);
        
        if (!target.getSku().equals(updatedProduct.getSku()) 
                && productRepository.existsBySku(updatedProduct.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }
        
        target.setSku(updatedProduct.getSku());
        target.setName(updatedProduct.getName());
        target.setDescription(updatedProduct.getDescription());
        target.setCategory(updatedProduct.getCategory());
        target.setSupplier(updatedProduct.getSupplier());
        target.setUnitPrice(updatedProduct.getUnitPrice());
        target.setReorderLevel(updatedProduct.getReorderLevel());
        target.setActive(updatedProduct.getActive());
        
        return productRepository.save(target);
    }

    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }
}