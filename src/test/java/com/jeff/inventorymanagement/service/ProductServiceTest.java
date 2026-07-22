package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.ProductRequest;
import com.jeff.inventorymanagement.dto.ProductResponse;
import com.jeff.inventorymanagement.entity.Category;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.Supplier;
import com.jeff.inventorymanagement.repository.CategoryRepository;
import com.jeff.inventorymanagement.repository.ProductRepository;
import com.jeff.inventorymanagement.repository.SupplierRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private ProductService productService;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Category category;
    private Supplier supplier;
    private AtomicLong productIdSequence;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Widgets");

        supplier = new Supplier();
        supplier.setId(10L);
        supplier.setName("Acme Supplies");

        productIdSequence = new AtomicLong(100L);
    }

    @Test
    void saveFromRequest_createsProductAndReturnsResponse() {
        ProductRequest request = makeValidRequest();
        when(productRepository.existsBySku("WDG-100")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(supplierRepository.findById(10L)).thenReturn(Optional.of(supplier));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(productIdSequence.getAndIncrement());
            return product;
        });

        ProductResponse response = productService.saveFromRequest(request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getSku()).isEqualTo("WDG-100");
        assertThat(response.getName()).isEqualTo("Widget");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getCategoryName()).isEqualTo("Widgets");
        assertThat(response.getSupplierId()).isEqualTo(10L);
        assertThat(response.getSupplierName()).isEqualTo("Acme Supplies");
        assertThat(response.getUnitPrice()).isEqualByComparingTo("19.99");
        assertThat(response.getActive()).isTrue();
    }

    @Test
    void saveFromRequest_rejectsDuplicateSku() {
        ProductRequest request = makeValidRequest();
        when(productRepository.existsBySku("WDG-100")).thenReturn(true);

        assertThatThrownBy(() -> productService.saveFromRequest(request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException statusException = (ResponseStatusException) ex;
                assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(statusException.getReason()).isEqualTo("SKU already exists");
            });

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateFromRequest_rejectsUnknownCategory() {
        Product existing = new Product();
        existing.setId(100L);
        existing.setSku("WDG-100");
        existing.setName("Widget");
        existing.setUnitPrice(new BigDecimal("19.99"));
        existing.setActive(true);

        ProductRequest request = makeValidRequest();
        request.setCategoryId(999L);

        when(productRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateFromRequest(100L, request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException statusException = (ResponseStatusException) ex;
                assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(statusException.getReason()).isEqualTo("Category not found");
            });

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void productRequest_rejectsNegativeUnitPrice() {
        ProductRequest request = makeValidRequest();
        request.setUnitPrice(new BigDecimal("-0.01"));

        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations)
            .anyMatch(v -> v.getPropertyPath().toString().equals("unitPrice")
                && v.getMessage().equals("Unit price cannot be negative"));
    }

    @Test
    void findAllAsResponse_filtersProductsAndMapsSortColumns() {
        Product product = new Product();
        product.setId(100L);
        product.setSku("WDG-100");
        product.setName("Widget");
        product.setDescription("A standard widget");
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setUnitPrice(new BigDecimal("19.99"));
        product.setActive(true);

        Pageable clientPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "unitPrice"));
        Pageable expectedDbPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "unit_price"));
        when(productRepository.findFiltered(eq("widget"), eq(1L), eq(10L), eq(true), eq(expectedDbPageable)))
            .thenReturn(new PageImpl<>(List.of(product), expectedDbPageable, 1));

        Page<ProductResponse> page = productService.findAllAsResponse(
            "widget", 1L, 10L, true, clientPageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        ProductResponse response = page.getContent().get(0);
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getSku()).isEqualTo("WDG-100");
        assertThat(response.getName()).isEqualTo("Widget");
        assertThat(response.getCategoryId()).isEqualTo(1L);
        assertThat(response.getSupplierId()).isEqualTo(10L);
        assertThat(response.getActive()).isTrue();

        verify(productRepository).findFiltered("widget", 1L, 10L, true, expectedDbPageable);
    }

    private ProductRequest makeValidRequest() {
        ProductRequest request = new ProductRequest();
        request.setSku("WDG-100");
        request.setName("Widget");
        request.setDescription("A standard widget");
        request.setCategoryId(1L);
        request.setSupplierId(10L);
        request.setUnitPrice(new BigDecimal("19.99"));
        request.setActive(true);
        return request;
    }
}
