package se.cockroachdb.order.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import se.cockroachdb.order.annotation.Remark;
import se.cockroachdb.order.annotation.TransactionExplicit;
import se.cockroachdb.order.domain.Order;
import se.cockroachdb.order.repository.OrderRepository;
import se.cockroachdb.order.repository.ProductRepository;
import se.cockroachdb.order.util.Assertions;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @TransactionExplicit
    public void placeOrders(List<Order> orders) {
        Assertions.assertTransaction();

        orders.forEach(order -> {
            order.getOrderItems().forEach(orderItem -> {
                inventoryService.increaseProductInventory(
                        orderItem.getProduct().getId(), orderItem.getQuantity());
            });
        });
        orderRepository.saveAll(orders);
    }

    @Autowired
    private InventoryService inventoryService;

    @TransactionExplicit
    @Remark("This is only to demonstrate invoking a boundary from other boundary (anti-pattern)")
    public void placeOrdersBadly(List<Order> orders) {
        Assertions.assertTransaction();

        orders.forEach(order -> {
            order.getOrderItems().forEach(orderItem -> {
                // This is also a boundary (avoid this)
                inventoryService.increaseProductInventoryBadly(
                        orderItem.getProduct().getId(), orderItem.getQuantity());
            });
        });

        orderRepository.saveAll(orders);
    }

    public BigDecimal getTotalOrderCost() {
        return orderRepository.totalOrderCost();
    }
}
