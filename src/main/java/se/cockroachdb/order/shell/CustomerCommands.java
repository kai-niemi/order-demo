package se.cockroachdb.order.shell;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.BeanListTableModel;

import se.cockroachdb.order.domain.Customer;
import se.cockroachdb.order.service.OrderFacade;

@ShellComponent
@ShellCommandGroup("02. Customer Domain Aggregate")
public class CustomerCommands extends AbstractInteractiveCommand {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderFacade orderFacade;

    @ShellMethod(value = "Create customers", key = {"create-customers", "cc"})
    public void createCustomers(@ShellOption(help = "number of customers", defaultValue = "10") Integer count) {
        logger.info("Creating %d customers...".formatted(count));
        orderFacade.createCustomers(count);
    }

    @ShellMethod(value = "List customers", key = {"list-customers", "lc"})
    public void listCustomers(
            @ShellOption(help = "page number", defaultValue = "0") Integer pageNumber,
            @ShellOption(help = "page size", defaultValue = "10") Integer pageSize) {
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("id", "Id");
        header.put("userName", "User Name");
        header.put("firstName", "First Name");
        header.put("lastName", "Last Name");

        Pageable page = PageRequest.of(pageNumber, pageSize);

        while (page.isPaged()) {
            Page<Customer> customerPage = orderFacade.findCustomers(page);

            logger.info("\n" + customerPage.toString());
            logger.info("\n" + prettyPrint(new BeanListTableModel<>(customerPage.getContent(), header)));

            page = askForPage(customerPage).orElseGet(Pageable::unpaged);
        }
    }
}
