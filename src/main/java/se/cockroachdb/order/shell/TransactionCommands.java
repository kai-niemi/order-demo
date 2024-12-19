package se.cockroachdb.order.shell;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DurationFormat;
import org.springframework.format.datetime.standard.DurationFormatterUtils;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import se.cockroachdb.order.aspect.InjectionPolicy;
import se.cockroachdb.order.aspect.TransactionDelayAspect;
import se.cockroachdb.order.aspect.TransactionDelay;
import se.cockroachdb.order.service.OrderFacade;

@ShellComponent
@ShellCommandGroup("01. Transaction Admin")
public class TransactionCommands {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderFacade orderFacade;

    @ShellMethod(value = "Set explicit transaction boundary synthetic delay", key = {"explicit-delay", "ed"})
    public void setExplicitTransactionDelay(
            @ShellOption(help = "transaction delay in duration format (2m15s etc)",
                    defaultValue = "5s") String duration,
            @ShellOption(help = "transaction delay injection policy",
                    valueProvider = EnumValueProvider.class) InjectionPolicy policy
    ) {
        Duration d = DurationFormatterUtils.parse(duration, DurationFormat.Style.SIMPLE);
        TransactionDelayAspect.setExplicitTransactionDelay(new TransactionDelay(d, policy));
        logger.info("Applying explicit transaction delay: %s"
                .formatted(TransactionDelayAspect.getExplicitTransactionDelay()));
    }

    @ShellMethod(value = "Set implicit transaction boundary synthetic delay", key = {"implicit-delay", "id"})
    public void setImplicitTransactionDelay(
            @ShellOption(help = "transaction delay in duration format (2m15s etc)",
                    defaultValue = "5s") String duration,
            @ShellOption(help = "transaction delay injection policy",
                    valueProvider = EnumValueProvider.class) InjectionPolicy policy
    ) {
        Duration d = DurationFormatterUtils.parse(duration, DurationFormat.Style.SIMPLE);
        TransactionDelayAspect.setImplicitTransactionDelay(new TransactionDelay(d, policy));
        logger.info("Applying implicit transaction delay: %s"
                .formatted(TransactionDelayAspect.getImplicitTransactionDelay()));
    }

    @ShellMethod(value = "Print current transaction delays", key = {"print-delays", "pd"})
    public void printDelays() {
        logger.info("Implicit:%n%s".formatted(TransactionDelayAspect.getImplicitTransactionDelay()));
        logger.info("Explicit:%n%s".formatted(TransactionDelayAspect.getExplicitTransactionDelay()));
    }

    @ShellMethod(value = "Clear current transaction delays", key = {"clear-delays", "cd"})
    public void clearDelays() {
        TransactionDelayAspect.clear();
        logger.info("Cleared transaction delays...");
    }

    @ShellMethod(value = "Purge all data", key = {"purge", "wipe"})
    public void purgeAll(@ShellOption(help = "confirm") Boolean confirm) {
        if (confirm) {
            logger.info("Purge all order data...");
            orderFacade.purgeAll();
        } else {
            logger.warn("Please confirm");
        }
    }
}
