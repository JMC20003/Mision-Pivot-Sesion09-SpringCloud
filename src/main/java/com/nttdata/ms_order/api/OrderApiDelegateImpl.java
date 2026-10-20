package com.nttdata.ms_order.api;

import com.nttdata.ms_order.model.CreateOrderItem;
import com.nttdata.ms_order.model.CreateOrderRequest;
import com.nttdata.ms_order.model.Order;
import com.nttdata.ms_order.model.OrderItem;
import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OrderApiDelegateImpl implements ApiApiDelegate {

    private static final Map<Long, CustomerDetails> CLIENTES_DEMO = Map.of(
            1L, new CustomerDetails("Juan", "Pérez"));

    private final List<Order> ordenesEnMemoria = new CopyOnWriteArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public ResponseEntity<Order> createOrder(CreateOrderRequest request) {
        if (!isValid(request)) {
            return ResponseEntity.badRequest().build();
        }
        CustomerDetails customer = CLIENTES_DEMO.get(request.getCustomerId());
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }

        List<OrderItem> items = request.getItems().stream()
                .map(this::toOrderItem)
                .toList();
        double total = items.stream()
                .map(item -> BigDecimal.valueOf(item.getSubtotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();

        String orderId = Long.toString(idGenerator.getAndIncrement());
        Order order = new Order()
                .id(orderId)
                .customerId(request.getCustomerId())
                .customerName(customer.name())
                .customerLastName(customer.lastName())
                .items(items)
                .total(total)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC));

        ordenesEnMemoria.add(order);
        return ResponseEntity.created(URI.create("/api/orders/" + orderId)).body(order);
    }

    @Override
    public ResponseEntity<List<Order>> getOrders() {
        return ResponseEntity.ok(List.copyOf(ordenesEnMemoria));
    }

    @Override
    public ResponseEntity<Void> deleteOrder(String orderId) {
        boolean deleted = ordenesEnMemoria.removeIf(order -> order.getId().equals(orderId));
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    private boolean isValid(CreateOrderRequest request) {
        return request != null
                && request.getCustomerId() != null
                && request.getCustomerId() > 0
                && request.getItems() != null
                && !request.getItems().isEmpty()
                && request.getItems().stream().allMatch(item ->
                        item != null
                                && item.getProductId() != null
                                && !item.getProductId().isBlank()
                                && item.getQuantity() != null
                                && item.getQuantity() > 0);
    }

    private OrderItem toOrderItem(CreateOrderItem requestedItem) {
        String productId = requestedItem.getProductId();
        int quantity = requestedItem.getQuantity();
        BigDecimal unitPrice = "1".equals(productId)
                ? new BigDecimal("2500.69")
                : BigDecimal.ZERO;
        String productName = "1".equals(productId) ? "Celular" : "Producto " + productId;
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return new OrderItem()
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice.doubleValue())
                .subtotal(subtotal.doubleValue());
    }

    private record CustomerDetails(String name, String lastName) {
    }
}
