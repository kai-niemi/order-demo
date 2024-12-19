package se.cockroachdb.order;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {
        TransactionAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        DataSourceAutoConfiguration.class
})
@EnableJdbcRepositories(basePackageClasses = {Application.class})
@ComponentScan(basePackageClasses = Application.class)
public class Application implements PromptProvider {
    @Override
    public AttributedString getPrompt() {
        AttributedStringBuilder sb = new AttributedStringBuilder();
        sb.append("orders: ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
        return sb.toAttributedString();
    }

    public static void main(String[] args) {
        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        LinkedList<String> passThroughArgs = new LinkedList<>();

        Set<String> profiles =
                StringUtils.commaDelimitedListToSet(System.getProperty("spring.profiles.active"));

        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.equals("--verbose")) {
                profiles.add("verbose");
            } else if (arg.equals("--profiles")) {
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(argsList.pop()));
            } else {
                if (arg.startsWith("--") || arg.startsWith("@")) {
                    passThroughArgs.add(arg);
                } else {
                    profiles.add(arg);
                }
            }
        }

        if (!profiles.isEmpty()) {
            System.setProperty("spring.profiles.active", String.join(",", profiles));
        }

        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.NONE)
                .logStartupInfo(true)
                .profiles(profiles.toArray(new String[0]))
                .run(passThroughArgs.toArray(new String[] {}));
    }
}

