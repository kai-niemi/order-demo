package se.cockroachdb.order.shell;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.BeanListTableModel;

import se.cockroachdb.order.domain.Order;
import se.cockroachdb.order.service.OrderFacade;

@ShellComponent
@ShellCommandGroup("04. Order Domain Aggregate")
public class OrderCommands extends AbstractInteractiveCommand {
    @Autowired
    private OrderFacade orderFacade;

    @ShellMethod(value = "Create purchase orders", key = {"create-orders", "co"})
    public void createOrders(@ShellOption(help = "number of orders", defaultValue = "10") Integer count,
                             @ShellOption(help = "order tags", defaultValue = "random") String tags,
                             @ShellOption(help = "use bad transaction semantics") Boolean badly,
                             @ShellOption(help = "transaction completion delay in seconds", defaultValue = "0") Integer idleTime
    ) {
        logger.info("Creating %d orders...".formatted(count));
        orderFacade.createOrders(count, tags, badly);
    }

    @ShellMethod(value = "List purchase orders", key = {"list-orders", "lo"})
    public void listOrders(
            @ShellOption(help = "page number", defaultValue = "0") Integer pageNumber,
            @ShellOption(help = "page size", defaultValue = "10") Integer pageSize) {
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("id", "Id");
        header.put("totalPrice", "Price");
        header.put("placedAt", "Placed At");
        header.put("tags", "Tags");
        header.put("updatedAt", "Updated At");
        header.put("status", "Status");

        Pageable page = PageRequest.of(pageNumber, pageSize);

        while (page.isPaged()) {
            Page<Order> orderPage = orderFacade.findOrders(page);

            logger.info("\n" + orderPage.toString());
            logger.info("\n" + prettyPrint(new BeanListTableModel<>(orderPage.getContent(), header)));

            page = askForPage(orderPage).orElseGet(Pageable::unpaged);
        }
    }

    @ShellMethod(value = "List purchase order items", key = {"list-items", "li"})
    public void listOrderItems(@ShellOption(help = "order id", valueProvider = OrderValueProvider.class) String orderId) {
        Order order = orderFacade.findOrderById(UUID.fromString(orderId));

        AtomicInteger idx = new AtomicInteger();
        logger.info("\n" + prettyPrint(
                new ListTableModel<>(order.getOrderItems(),
                        List.of("#", "Qty", "Unit Price", "Product"), (object, column) -> {
                    return switch (column) {
                        case 0 -> idx.incrementAndGet();
                        case 1 -> object.getQuantity();
                        case 2 -> object.getUnitPrice();
                        case 3 -> object.getProduct().getId();
                        default -> "??";
                    };
                })));
    }

    @ShellMethod(value = "List purchase orders by customer ID", key = {"list-customer-orders", "lco"})
    public void listCustomerOrders(
            @ShellOption(help = "customer id", valueProvider = CustomerValueProvider.class) String customerId) {
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("id", "Id");
        header.put("totalPrice", "Price");
        header.put("placedAt", "Placed At");
        header.put("tags", "Tags");
        header.put("updatedAt", "Updated At");
        header.put("status", "Status");

        Iterable<Order> orders = orderFacade.findOrdersByCustomerId(UUID.fromString(customerId));
        logger.info("\n" + prettyPrint(new BeanListTableModel<>(orders, header)));
    }

    @ShellMethod(value = "Report order total", key = {"report", "r"})
    public void reportTotals() {
        logger.info("Total order cost: {}", orderFacade.getTotalOrderCost());
    }
}
