package se.cockroachdb.order.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.cockroachdb.order.domain.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, UUID>,
        PagingAndSortingRepository<Order, UUID> {
    @Query(value = "select o.* from orders o "
                   + "join customer c on o.customer_id = c.id "
                   + "where c.id=:customerId")
    Iterable<Order> findOrdersByCustomerId(@Param("customerId") UUID customerId);

    @Query(value = "select sum(o.total_price) from orders o")
    BigDecimal totalOrderCost();
}
