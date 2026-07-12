package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.ProductRequest;
import com.jeff.inventorymanagement.dto.ProductResponse;
import com.jeff.inventorymanagement.entity.Category;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.Supplier;
import com.jeff.inventorymanagement.repository.CategoryRepository;
import com.jeff.inventorymanagement.repository.ProductRepository;
import com.jeff.inventorymanagement.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
public class ProductService {
    private static final Map<String, String> SORT_COLUMNS = Map.of(
        "unitPrice", "unit_price",
        "categoryId", "category_id",
        "supplierId", "supplier_id"
    );
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
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
        target.setActive(updatedProduct.getActive());
        
        return productRepository.save(target);
    }

    public void delete(Long id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getSupplier() != null) {
            response.setSupplierId(product.getSupplier().getId());
            response.setSupplierName(product.getSupplier().getName());
        }
        
        response.setUnitPrice(product.getUnitPrice());
        response.setActive(product.getActive());
        return response;
    }

    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            product.setCategory(category);
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier not found"));
            product.setSupplier(supplier);
        }
        
        product.setUnitPrice(request.getUnitPrice());
        product.setActive(request.getActive());
        return product;
    }

    public List<ProductResponse> findAllAsResponse() {
        return findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public Page<ProductResponse> findAllAsResponse(String search, Long categoryId, Long supplierId, Boolean active, Pageable pageable) {
        return productRepository.findFiltered(search, categoryId, supplierId, active, toDatabasePageable(pageable))
            .map(this::toResponse);
    }

    // Native SQL sorts by column names (unit_price), but clients send Java field names (unitPrice).
    private Pageable toDatabasePageable(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }

        Sort dbSort = Sort.by(pageable.getSort().stream()
            .map(order -> {
                String column = SORT_COLUMNS.getOrDefault(order.getProperty(), order.getProperty());
                return new Sort.Order(order.getDirection(), column);
            })
            .toList());

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), dbSort);
    }

    public ProductResponse findByIdAsResponse(Long id) {
        return toResponse(findById(id));
    }

    public ProductResponse saveFromRequest(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }
        Product product = toEntity(request);
        return toResponse(productRepository.save(product));
    }

    public ProductResponse updateFromRequest(Long id, ProductRequest request) {
        Product target = findById(id);
        
        if (!target.getSku().equals(request.getSku()) 
                && productRepository.existsBySku(request.getSku())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists");
        }
        
        target.setSku(request.getSku());
        target.setName(request.getName());
        target.setDescription(request.getDescription());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
            target.setCategory(category);
        } else {
            target.setCategory(null);
        }
        
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Supplier not found"));
            target.setSupplier(supplier);
        } else {
            target.setSupplier(null);
        }
        
        target.setUnitPrice(request.getUnitPrice());
        target.setActive(request.getActive());
        
        return toResponse(productRepository.save(target));
    }
}