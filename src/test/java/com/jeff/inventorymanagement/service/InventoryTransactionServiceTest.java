package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.InventoryTransactionResponse;
import com.jeff.inventorymanagement.dto.StockAdjustRequest;
import com.jeff.inventorymanagement.dto.StockInOutRequest;
import com.jeff.inventorymanagement.dto.StockTransferRequest;
import com.jeff.inventorymanagement.entity.Inventory;
import com.jeff.inventorymanagement.entity.InventoryTransaction;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.TransactionType;
import com.jeff.inventorymanagement.repository.InventoryRepository;
import com.jeff.inventorymanagement.repository.InventoryTransactionRepository;
import com.jeff.inventorymanagement.repository.LocationRepository;
import com.jeff.inventorymanagement.repository.ProductRepository;
import com.jeff.inventorymanagement.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceTest {

    @Mock
    private InventoryService inventoryService;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InventoryTransactionService inventoryTransactionService;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Product product;
    private Location location;
    private Location destinationLocation;
    private AtomicLong transactionIdSequence;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setName("Widget");

        location = new Location();
        location.setId(10L);
        location.setName("Warehouse A");

        destinationLocation = new Location();
        destinationLocation.setId(20L);
        destinationLocation.setName("Warehouse B");

        transactionIdSequence = new AtomicLong(100L);

        lenient().when(inventoryTransactionRepository.save(any(InventoryTransaction.class)))
            .thenAnswer(invocation -> {
                InventoryTransaction transaction = invocation.getArgument(0);
                if (transaction.getId() == null) {
                    transaction.setId(transactionIdSequence.getAndIncrement());
                }
                return transaction;
            });
    }

    @Test
    void stockIn_increasesExistingInventory() {
        Inventory inventory = makeInventory(50);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryTransactionResponse response = inventoryTransactionService.stockIn(makeStockInOutRequest(25));

        assertThat(inventory.getQuantity()).isEqualTo(75);
        assertThat(response.getPreviousQuantity()).isEqualTo(50);
        assertThat(response.getNewQuantity()).isEqualTo(75);
        assertThat(response.getQuantityChange()).isEqualTo(25);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.STOCK_IN);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void stockIn_createsInventoryWhenNoneExists() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(99L);
            }
            return saved;
        });

        InventoryTransactionResponse response = inventoryTransactionService.stockIn(makeStockInOutRequest(40));

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository, atLeastOnce()).save(inventoryCaptor.capture());
        Inventory savedInventory = inventoryCaptor.getAllValues().get(inventoryCaptor.getAllValues().size() - 1);

        assertThat(savedInventory.getProduct()).isEqualTo(product);
        assertThat(savedInventory.getLocation()).isEqualTo(location);
        assertThat(savedInventory.getQuantity()).isEqualTo(40);
        assertThat(response.getPreviousQuantity()).isEqualTo(0);
        assertThat(response.getNewQuantity()).isEqualTo(40);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.STOCK_IN);
    }

    @Test
    void stockOut_decreasesInventory() {
        Inventory inventory = makeInventory(50);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryTransactionResponse response = inventoryTransactionService.stockOut(makeStockInOutRequest(20));

        assertThat(inventory.getQuantity()).isEqualTo(30);
        assertThat(response.getPreviousQuantity()).isEqualTo(50);
        assertThat(response.getNewQuantity()).isEqualTo(30);
        assertThat(response.getQuantityChange()).isEqualTo(20);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.STOCK_OUT);
    }

    @Test
    void stockOut_rejectsInsufficientStock() {
        Inventory inventory = makeInventory(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryTransactionService.stockOut(makeStockInOutRequest(25)))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("Insufficient stock");
            });

        assertThat(inventory.getQuantity()).isEqualTo(10);
        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(inventoryTransactionRepository, never()).save(any(InventoryTransaction.class));
    }

    @Test
    void adjust_correctlyIncreasesStock() {
        Inventory inventory = makeInventory(30);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryTransactionResponse response = inventoryTransactionService.adjust(makeAdjustRequest(55));

        assertThat(inventory.getQuantity()).isEqualTo(55);
        assertThat(response.getPreviousQuantity()).isEqualTo(30);
        assertThat(response.getNewQuantity()).isEqualTo(55);
        assertThat(response.getQuantityChange()).isEqualTo(25);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.ADJUSTMENT);
    }

    @Test
    void adjust_correctlyDecreasesStock() {
        Inventory inventory = makeInventory(30);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        InventoryTransactionResponse response = inventoryTransactionService.adjust(makeAdjustRequest(12));

        assertThat(inventory.getQuantity()).isEqualTo(12);
        assertThat(response.getPreviousQuantity()).isEqualTo(30);
        assertThat(response.getNewQuantity()).isEqualTo(12);
        assertThat(response.getQuantityChange()).isEqualTo(18);
        assertThat(response.getTransactionType()).isEqualTo(TransactionType.ADJUSTMENT);
    }

    @Test
    void adjust_rejectsResultBelowZero() {
        StockAdjustRequest request = makeAdjustRequest(-1);

        Set<ConstraintViolation<StockAdjustRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
            .anyMatch(v -> v.getPropertyPath().toString().equals("newQuantity")
                && v.getMessage().equals("New quantity must be 0 or greater"));
    }

    @Test
    void transfer_decreasesSourceAndIncreasesDestination() {
        Inventory sourceInventory = makeInventory(50);
        Inventory destinationInventory = makeInventoryAt(destinationLocation, 10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(locationRepository.findById(20L)).thenReturn(Optional.of(destinationLocation));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(sourceInventory));
        when(inventoryService.findByProductIdAndLocationId(1L, 20L)).thenReturn(Optional.of(destinationInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<InventoryTransactionResponse> responses =
            inventoryTransactionService.transfer(makeTransferRequest(15));

        assertThat(sourceInventory.getQuantity()).isEqualTo(35);
        assertThat(destinationInventory.getQuantity()).isEqualTo(25);
        assertThat(responses).hasSize(2);

        InventoryTransactionResponse transferOut = responses.get(0);
        InventoryTransactionResponse transferIn = responses.get(1);

        assertThat(transferOut.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(transferOut.getLocationId()).isEqualTo(10L);
        assertThat(transferOut.getPreviousQuantity()).isEqualTo(50);
        assertThat(transferOut.getNewQuantity()).isEqualTo(35);
        assertThat(transferOut.getQuantityChange()).isEqualTo(15);

        assertThat(transferIn.getTransactionType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(transferIn.getLocationId()).isEqualTo(20L);
        assertThat(transferIn.getPreviousQuantity()).isEqualTo(10);
        assertThat(transferIn.getNewQuantity()).isEqualTo(25);
        assertThat(transferIn.getQuantityChange()).isEqualTo(15);
        assertTransferRecordsAreRelated(transferOut, transferIn);
    }

    @Test
    void transfer_createsDestinationInventoryWhenNoneExists() {
        Inventory sourceInventory = makeInventory(50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(locationRepository.findById(20L)).thenReturn(Optional.of(destinationLocation));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(sourceInventory));
        when(inventoryService.findByProductIdAndLocationId(1L, 20L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(99L);
            }
            return saved;
        });

        List<InventoryTransactionResponse> responses =
            inventoryTransactionService.transfer(makeTransferRequest(15));

        assertThat(sourceInventory.getQuantity()).isEqualTo(35);
        assertThat(responses).hasSize(2);

        InventoryTransactionResponse transferOut = responses.get(0);
        InventoryTransactionResponse transferIn = responses.get(1);

        assertThat(transferOut.getTransactionType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(transferOut.getLocationId()).isEqualTo(10L);
        assertThat(transferOut.getPreviousQuantity()).isEqualTo(50);
        assertThat(transferOut.getNewQuantity()).isEqualTo(35);
        assertThat(transferOut.getQuantityChange()).isEqualTo(15);

        assertThat(transferIn.getTransactionType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(transferIn.getLocationId()).isEqualTo(20L);
        assertThat(transferIn.getPreviousQuantity()).isEqualTo(0);
        assertThat(transferIn.getNewQuantity()).isEqualTo(15);
        assertThat(transferIn.getQuantityChange()).isEqualTo(15);
        assertTransferRecordsAreRelated(transferOut, transferIn);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository, atLeastOnce()).save(inventoryCaptor.capture());
        assertThat(inventoryCaptor.getAllValues())
            .anySatisfy(saved -> {
                assertThat(saved.getLocation()).isEqualTo(destinationLocation);
                assertThat(saved.getProduct()).isEqualTo(product);
                assertThat(saved.getQuantity()).isEqualTo(15);
            });
    }

    @Test
    void transfer_rejectsInsufficientSourceStock() {
        Inventory sourceInventory = makeInventory(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));
        when(locationRepository.findById(20L)).thenReturn(Optional.of(destinationLocation));
        when(inventoryService.findByProductIdAndLocationId(1L, 10L)).thenReturn(Optional.of(sourceInventory));

        assertThatThrownBy(() -> inventoryTransactionService.transfer(makeTransferRequest(15)))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("Insufficient stock");
            });

        assertThat(sourceInventory.getQuantity()).isEqualTo(5);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void transfer_rejectsIdenticalSourceAndDestinationLocations() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(locationRepository.findById(10L)).thenReturn(Optional.of(location));

        StockTransferRequest request = new StockTransferRequest();
        request.setProductId(1L);
        request.setFromLocationId(10L);
        request.setToLocationId(10L);
        request.setQuantity(5);
        request.setReason("Invalid transfer");

        assertThatThrownBy(() -> inventoryTransactionService.transfer(request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason())
                    .isEqualTo("Source and destination locations must be different");
            });

        verify(inventoryService, never()).findByProductIdAndLocationId(any(), any());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    private void assertTransferRecordsAreRelated(
            InventoryTransactionResponse transferOut,
            InventoryTransactionResponse transferIn) {
        assertThat(transferOut.getId()).isNotNull();
        assertThat(transferIn.getId()).isNotNull();
        assertThat(transferOut.getRelatedTransactionId()).isEqualTo(transferIn.getId());
        assertThat(transferIn.getRelatedTransactionId()).isEqualTo(transferOut.getId());
    }

    private Inventory makeInventory(int quantity) {
        return makeInventoryAt(location, quantity);
    }

    private Inventory makeInventoryAt(Location inventoryLocation, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setId(inventoryLocation.getId());
        inventory.setProduct(product);
        inventory.setLocation(inventoryLocation);
        inventory.setQuantity(quantity);
        return inventory;
    }

    private StockInOutRequest makeStockInOutRequest(int quantity) {
        StockInOutRequest request = new StockInOutRequest();
        request.setProductId(1L);
        request.setLocationId(10L);
        request.setQuantity(quantity);
        request.setReason("Test stock movement");
        return request;
    }

    private StockAdjustRequest makeAdjustRequest(int newQuantity) {
        StockAdjustRequest request = new StockAdjustRequest();
        request.setProductId(1L);
        request.setLocationId(10L);
        request.setNewQuantity(newQuantity);
        request.setReason("Test adjustment");
        return request;
    }

    private StockTransferRequest makeTransferRequest(int quantity) {
        StockTransferRequest request = new StockTransferRequest();
        request.setProductId(1L);
        request.setFromLocationId(10L);
        request.setToLocationId(20L);
        request.setQuantity(quantity);
        request.setReason("Test transfer");
        return request;
    }
}
