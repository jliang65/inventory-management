package com.jeff.inventorymanagement.service;

import com.jeff.inventorymanagement.dto.InventoryTransactionResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderItemRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderItemResponse;
import com.jeff.inventorymanagement.dto.PurchaseOrderRequest;
import com.jeff.inventorymanagement.dto.PurchaseOrderResponse;
import com.jeff.inventorymanagement.dto.StockInOutRequest;
import com.jeff.inventorymanagement.entity.Location;
import com.jeff.inventorymanagement.entity.Product;
import com.jeff.inventorymanagement.entity.PurchaseOrder;
import com.jeff.inventorymanagement.entity.PurchaseOrderItem;
import com.jeff.inventorymanagement.entity.PurchaseOrderStatus;
import com.jeff.inventorymanagement.entity.Supplier;
import com.jeff.inventorymanagement.repository.PurchaseOrderItemRepository;
import com.jeff.inventorymanagement.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderItemRepository purchaseOrderItemRepository;
    @Mock
    private SupplierService supplierService;
    @Mock
    private LocationService locationService;
    @Mock
    private ProductService productService;
    @Mock
    private InventoryTransactionService inventoryTransactionService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private Supplier supplier;
    private Location destinationLocation;
    private Product product;
    private AtomicLong orderIdSequence;
    private AtomicLong itemIdSequence;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Acme Supplies");

        destinationLocation = new Location();
        destinationLocation.setId(10L);
        destinationLocation.setName("Warehouse A");

        product = new Product();
        product.setId(100L);
        product.setName("Widget");
        product.setSku("WDG-100");
        product.setSupplier(supplier);

        orderIdSequence = new AtomicLong(1L);
        itemIdSequence = new AtomicLong(50L);

        lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
            .thenAnswer(invocation -> {
                PurchaseOrder order = invocation.getArgument(0);
                if (order.getId() == null) {
                    order.setId(orderIdSequence.getAndIncrement());
                }
                return order;
            });

        lenient().when(purchaseOrderItemRepository.save(any(PurchaseOrderItem.class)))
            .thenAnswer(invocation -> {
                PurchaseOrderItem item = invocation.getArgument(0);
                if (item.getId() == null) {
                    item.setId(itemIdSequence.getAndIncrement());
                }
                return item;
            });
    }

    @Test
    void createDraftPurchaseOrder() {
        when(supplierService.findById(1L)).thenReturn(supplier);
        when(locationService.findById(10L)).thenReturn(destinationLocation);
        when(purchaseOrderRepository.findById(1L)).thenAnswer(invocation -> {
            PurchaseOrder saved = new PurchaseOrder();
            saved.setId(1L);
            saved.setSupplier(supplier);
            saved.setDestinationLocation(destinationLocation);
            saved.setStatus(PurchaseOrderStatus.DRAFT);
            saved.setNotes("Initial draft");
            return Optional.of(saved);
        });
        when(purchaseOrderItemRepository.findByPurchaseOrderId(1L)).thenReturn(Collections.emptyList());

        PurchaseOrderResponse response = purchaseOrderService.saveFromRequest(makePurchaseOrderRequest());

        assertThat(response.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(response.getSupplierId()).isEqualTo(1L);
        assertThat(response.getDestinationLocationId()).isEqualTo(10L);
        assertThat(response.getNotes()).isEqualTo("Initial draft");
        assertThat(response.getItems()).isEmpty();

        ArgumentCaptor<PurchaseOrder> orderCaptor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(purchaseOrderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
    }

    @Test
    void rejectOrderWithInvalidSupplier() {
        when(supplierService.findById(999L))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));

        PurchaseOrderRequest request = makePurchaseOrderRequest();
        request.setSupplierId(999L);

        assertThatThrownBy(() -> purchaseOrderService.saveFromRequest(request))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(exception.getReason()).isEqualTo("Supplier not found");
            });

        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void addItemToDraftOrder() {
        PurchaseOrder draftOrder = makePurchaseOrder(1L, PurchaseOrderStatus.DRAFT);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));
        when(purchaseOrderItemRepository.findByPurchaseOrderId(1L)).thenReturn(Collections.emptyList());
        when(productService.findById(100L)).thenReturn(product);

        PurchaseOrderItemResponse response =
            purchaseOrderService.addItem(1L, makeItemRequest(100L, 5, "12.50"));

        assertThat(response.getProductId()).isEqualTo(100L);
        assertThat(response.getOrderedQuantity()).isEqualTo(5);
        assertThat(response.getUnitCost()).isEqualByComparingTo("12.50");
        assertThat(response.getLineTotal()).isEqualByComparingTo("62.50");

        ArgumentCaptor<PurchaseOrderItem> itemCaptor = ArgumentCaptor.forClass(PurchaseOrderItem.class);
        verify(purchaseOrderItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().getPurchaseOrder()).isEqualTo(draftOrder);
        assertThat(itemCaptor.getValue().getProduct()).isEqualTo(product);
    }

    @Test
    void rejectItemChangesAfterSubmission() {
        PurchaseOrder submittedOrder = makePurchaseOrder(1L, PurchaseOrderStatus.SUBMITTED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(submittedOrder));

        assertThatThrownBy(() -> purchaseOrderService.addItem(1L, makeItemRequest(100L, 5, "12.50")))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("Can only add items to draft orders");
            });

        verify(purchaseOrderItemRepository, never()).save(any(PurchaseOrderItem.class));
    }

    @Test
    void submitValidDraftOrder() {
        PurchaseOrder draftOrder = makePurchaseOrder(1L, PurchaseOrderStatus.DRAFT);
        PurchaseOrderItem item = makeOrderItem(draftOrder);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));
        when(purchaseOrderItemRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(item));

        PurchaseOrderResponse response = purchaseOrderService.submit(1L);

        assertThat(response.getStatus()).isEqualTo(PurchaseOrderStatus.SUBMITTED);
        assertThat(draftOrder.getStatus()).isEqualTo(PurchaseOrderStatus.SUBMITTED);
        assertThat(draftOrder.getOrderDate()).isNotNull();
        verify(purchaseOrderRepository).save(draftOrder);
    }

    @Test
    void rejectSubmissionOfEmptyOrder() {
        PurchaseOrder draftOrder = makePurchaseOrder(1L, PurchaseOrderStatus.DRAFT);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));
        when(purchaseOrderItemRepository.findByPurchaseOrderId(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> purchaseOrderService.submit(1L))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("Cannot submit an order with no items");
            });

        assertThat(draftOrder.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void receiveSubmittedOrderAndIncreaseInventory() {
        PurchaseOrder submittedOrder = makePurchaseOrder(1L, PurchaseOrderStatus.SUBMITTED);
        PurchaseOrderItem item = makeOrderItem(submittedOrder);
        item.setOrderedQuantity(25);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(submittedOrder));
        when(purchaseOrderItemRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(item));
        when(inventoryTransactionService.stockIn(any(StockInOutRequest.class)))
            .thenReturn(new InventoryTransactionResponse());

        PurchaseOrderResponse response = purchaseOrderService.receive(1L);

        assertThat(response.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(submittedOrder.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(submittedOrder.getReceivedAt()).isNotNull();

        ArgumentCaptor<StockInOutRequest> stockInCaptor = ArgumentCaptor.forClass(StockInOutRequest.class);
        verify(inventoryTransactionService).stockIn(stockInCaptor.capture());
        assertThat(stockInCaptor.getValue().getProductId()).isEqualTo(100L);
        assertThat(stockInCaptor.getValue().getLocationId()).isEqualTo(10L);
        assertThat(stockInCaptor.getValue().getQuantity()).isEqualTo(25);
        assertThat(stockInCaptor.getValue().getReason()).isEqualTo("Received from PO #1");
    }

    @Test
    void rejectReceivingOrderTwice() {
        PurchaseOrder receivedOrder = makePurchaseOrder(1L, PurchaseOrderStatus.RECEIVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(receivedOrder));

        assertThatThrownBy(() -> purchaseOrderService.receive(1L))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("Only submitted orders can be received");
            });

        verify(inventoryTransactionService, never()).stockIn(any(StockInOutRequest.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void cancelAllowedOrder() {
        PurchaseOrder submittedOrder = makePurchaseOrder(1L, PurchaseOrderStatus.SUBMITTED);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(submittedOrder));
        when(purchaseOrderItemRepository.findByPurchaseOrderId(1L)).thenReturn(Collections.emptyList());

        PurchaseOrderResponse response = purchaseOrderService.cancel(1L);

        assertThat(response.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        assertThat(submittedOrder.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        verify(purchaseOrderRepository).save(submittedOrder);
    }

    @Test
    void rejectInvalidStatusTransition() {
        PurchaseOrder draftOrder = makePurchaseOrder(1L, PurchaseOrderStatus.DRAFT);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(draftOrder));

        assertThatThrownBy(() -> purchaseOrderService.receive(1L))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(ex -> {
                ResponseStatusException exception = (ResponseStatusException) ex;
                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("Only submitted orders can be received");
            });

        assertThat(draftOrder.getStatus()).isEqualTo(PurchaseOrderStatus.DRAFT);
        verify(inventoryTransactionService, never()).stockIn(any(StockInOutRequest.class));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    private PurchaseOrder makePurchaseOrder(Long id, PurchaseOrderStatus status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setSupplier(supplier);
        order.setDestinationLocation(destinationLocation);
        order.setStatus(status);
        return order;
    }

    private PurchaseOrderItem makeOrderItem(PurchaseOrder order) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(50L);
        item.setPurchaseOrder(order);
        item.setProduct(product);
        item.setOrderedQuantity(5);
        item.setUnitCost(new BigDecimal("12.50"));
        return item;
    }

    private PurchaseOrderRequest makePurchaseOrderRequest() {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(1L);
        request.setDestinationLocationId(10L);
        request.setNotes("Initial draft");
        return request;
    }

    private PurchaseOrderItemRequest makeItemRequest(Long productId, int quantity, String unitCost) {
        PurchaseOrderItemRequest request = new PurchaseOrderItemRequest();
        request.setProductId(productId);
        request.setOrderedQuantity(quantity);
        request.setUnitCost(new BigDecimal(unitCost));
        return request;
    }
}
