package se.cockroachdb.order.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import se.cockroachdb.order.annotation.Remark;
import se.cockroachdb.order.annotation.TransactionExplicit;
import se.cockroachdb.order.annotation.TransactionSupports;
import se.cockroachdb.order.domain.Product;
import se.cockroachdb.order.repository.ProductRepository;
import se.cockroachdb.order.util.Assertions;

@Service
public class InventoryService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ProductRepository productRepository;

    @TransactionSupports
    @Remark("This is highly inefficient (full scan), equivalent to 'order by random()'")
    public List<Product> findRandomProducts(int count) {
        long qty = productRepository.count();
        int offset = ThreadLocalRandom.current().nextInt(Math.max(1, (int) qty - count)) / count;
        return productRepository.findAll(
                PageRequest.of(Math.max(0, offset), count)).getContent();
    }

    @TransactionSupports
    public void increaseProductInventory(UUID productId, int qty) {
        Product p = productRepository.findProductByIdForUpdate(productId).orElseThrow();
        p.addInventoryQuantity(qty);
        p.markNotNew();

        productRepository.save(p);
    }

    @TransactionExplicit
    @Remark("This is only to demonstrate invoking a boundary from other boundary (anti-pattern)")
    public void increaseProductInventoryBadly(UUID productId, int qty) {
        Assertions.assertTransaction();
        increaseProductInventory(productId, qty);
    }
}
