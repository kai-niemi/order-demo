package se.cockroachdb.order.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "orders")
public class Order extends AbstractEntity<UUID> {
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Customer customer;

        private final List<OrderItem> orderItems = new ArrayList<>();

        private String tags;

        private Builder() {
        }

        public Builder withCustomer(Customer customer) {
            this.customer = customer;
            return this;
        }

        public Builder withTags(String tags) {
            this.tags = tags;
            return this;
        }

        public OrderItem.Builder andOrderItem() {
            return new OrderItem.Builder(this, orderItems::add);
        }

        public Order build() {
            if (this.customer == null) {
                throw new IllegalStateException("Missing customer");
            }
            if (this.orderItems.isEmpty()) {
                throw new IllegalStateException("Empty order");
            }
            Order order = new Order();
            order.customer = AggregateReference.to(this.customer.getId());
            order.orderItems.addAll(this.orderItems);
            order.totalPrice = order.subTotal();
            order.placedAt = LocalDate.now();
            order.updatedAt = LocalDateTime.now();
            order.tags = tags;
            order.status = "PLACED";
            return order;
        }
    }

    @Id
    private UUID id;

    @Column(value = "total_price")
    private BigDecimal totalPrice;

    @Column(value = "placed_at")
    private LocalDate placedAt;

    @Column(value = "updated_at")
    private LocalDateTime updatedAt;

    @Column(value = "tags")
    private String tags;

    @Column(value = "status")
    private String status;

    @Column(value = "customer_id")
    private AggregateReference<Customer, UUID> customer;

    @MappedCollection(idColumn = "order_id", keyColumn = "order_id")
    private Set<OrderItem> orderItems = new HashSet<>();

    @Override
    public UUID getId() {
        return id;
    }

    public Order setId(UUID id) {
        this.id = id;
        return this;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDate getPlacedAt() {
        return placedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getTags() {
        return tags;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public AggregateReference<Customer, UUID> getCustomerRef() {
        return customer;
    }

    public Collection<OrderItem> getOrderItems() {
        return Collections.unmodifiableSet(orderItems);
    }

    public BigDecimal subTotal() {
        BigDecimal subTotal = BigDecimal.ZERO;
        for (OrderItem oi : orderItems) {
            subTotal = subTotal.add(oi.totalCost());
        }
        return subTotal;
    }
}
