package se.cockroachdb.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import se.cockroachdb.order.annotation.TransactionImplicit;
import se.cockroachdb.order.domain.Customer;
import se.cockroachdb.order.domain.Order;
import se.cockroachdb.order.domain.Product;
import se.cockroachdb.order.repository.CustomerRepository;
import se.cockroachdb.order.repository.OrderRepository;
import se.cockroachdb.order.repository.ProductRepository;
import se.cockroachdb.order.util.Assertions;
import se.cockroachdb.order.util.StreamUtils;

@Service
public class OrderFacadeImpl implements OrderFacade {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private InventoryService inventoryService;

    @Override
    @TransactionImplicit
    public void purgeAll() {
        Assertions.assertNoTransaction();

        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Override
    @TransactionImplicit
    public void createProductInventory(int numProducts) {
        Assertions.assertNoTransaction();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        StreamUtils.chunkedStream(IntStream.rangeClosed(1, numProducts)
                .mapToObj(x -> Product.builder()
                        .withName("CockroachDB Unleashed - First Edition")
                        .withPrice(new BigDecimal("19.95"))
                        .withSku(UUID.randomUUID().toString())
                        .withQuantity(random.nextInt(10, 20))
                        .build()
                ), 16).forEach(batch -> {
            productRepository.saveAll(batch);
        });
    }

    @Override
    @TransactionImplicit
    public void createCustomers(int numCustomers) {
        Assertions.assertNoTransaction();

        StreamUtils.chunkedStream(IntStream.rangeClosed(1, numCustomers)
                .mapToObj(x -> Customer.builder()
                        .withFirstName("Winston")
                        .withLastName("Atkinson")
                        .withUserName(UUID.randomUUID().toString())
                        .build()
                ), 16).forEach(batch -> {
            customerRepository.saveAll(batch);
        });
    }

    @Override
    @TransactionImplicit
    public void createOrders(int numOrders, String tags, boolean badly) {
        Assertions.assertNoTransaction();

        List<Product> productList = inventoryService.findRandomProducts(2);
        List<Customer> customerList = customerService.findRandomCustomers(1);

        Assert.isTrue(productList.size() >= 2, "not enough products");
        Assert.isTrue(!customerList.isEmpty(), "not enough customers");

        ThreadLocalRandom random = ThreadLocalRandom.current();

        StreamUtils.chunkedStream(IntStream.rangeClosed(1, numOrders)
                .mapToObj(x -> Order.builder()
                        .withCustomer(customerList.get(0))
                        .withTags(tags)
                        .andOrderItem()
                        .withProduct(productList.get(0))
                        .withQuantity(random.nextInt(1, 10))
                        .then()
                        .andOrderItem()
                        .withProduct(productList.get(1))
                        .withQuantity(random.nextInt(1, 10))
                        .then()
                        .build()
                ), 16).forEach(batch -> {

            if (badly) {
                orderService.placeOrdersBadly(batch);
            } else {
                orderService.placeOrders(batch);
            }
        });
    }

    @Override
    @TransactionImplicit
    public Page<Order> findOrders(Pageable pageable) {
        Assertions.assertNoTransaction();
        return orderRepository.findAll(pageable);
    }

    @Override
    @TransactionImplicit
    public Iterable<Order> findOrdersByCustomerId(UUID id) {
        Assertions.assertNoTransaction();
        return orderRepository.findOrdersByCustomerId(id);
    }

    @Override
    @TransactionImplicit
    public Page<Customer> findCustomers(Pageable pageable) {
        Assertions.assertNoTransaction();
        return customerRepository.findAll(pageable);
    }

    @Override
    @TransactionImplicit
    public Page<Product> findProducts(Pageable pageable) {
        Assertions.assertNoTransaction();
        return productRepository.findAll(pageable);
    }

    @Override
    @TransactionImplicit
    public BigDecimal getTotalOrderCost() {
        Assertions.assertNoTransaction();
        return orderService.getTotalOrderCost();
    }


    @Override
    @TransactionImplicit
    public Order findOrderById(UUID id) {
        Assertions.assertNoTransaction();
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No such entity found"));
    }
}
