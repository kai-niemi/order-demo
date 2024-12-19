package se.cockroachdb.order.shell;

import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.BeanListTableModel;

import se.cockroachdb.order.domain.Product;
import se.cockroachdb.order.service.OrderFacade;

@ShellComponent
@ShellCommandGroup("03. Inventory Domain Aggregate")
public class InventoryCommands extends AbstractInteractiveCommand {
    @Autowired
    private OrderFacade orderFacade;

    @ShellMethod(value = "Create product inventory", key = {"create-products", "cp"})
    public void createProducts(@ShellOption(help = "number of products", defaultValue = "10") Integer count) {
        logger.info("Creating %d products...".formatted(count));
        orderFacade.createProductInventory(count);
    }

    @ShellMethod(value = "List product inventory", key = {"list-products", "lp"})
    public void listProducts(
            @ShellOption(help = "page number", defaultValue = "0") Integer pageNumber,
            @ShellOption(help = "page size", defaultValue = "10") Integer pageSize) {
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("id", "Id");
        header.put("name", "Name");
        header.put("sku", "SKU");
        header.put("price", "Price");
        header.put("inventory", "Inventory");

        Pageable page = PageRequest.of(pageNumber, pageSize);

        while (page.isPaged()) {
            Page<Product> productPage = orderFacade.findProducts(page);

            logger.info("\n" + productPage.toString());
            logger.info("\n" + prettyPrint(new BeanListTableModel<>(productPage.getContent(), header)));

            page = askForPage(productPage).orElseGet(Pageable::unpaged);
        }
    }

}
