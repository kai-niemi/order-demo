package se.cockroachdb.order.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import se.cockroachdb.order.domain.Customer;
import se.cockroachdb.order.domain.Order;
import se.cockroachdb.order.domain.Product;

public interface OrderFacade {
    void createProductInventory(int numProducts);

    void createCustomers(int numCustomers);

    void createOrders(int numOrders, String tags, boolean badly);

    Page<Product> findProducts(Pageable pageable);

    Page<Customer> findCustomers(Pageable pageable);

    Page<Order> findOrders(Pageable pageable);

    Iterable<Order> findOrdersByCustomerId(UUID id);

    Order findOrderById(UUID id);

    BigDecimal getTotalOrderCost();

    void purgeAll();
}
