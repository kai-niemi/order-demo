package se.cockroachdb.order.shell;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.stereotype.Component;

import se.cockroachdb.order.domain.Order;
import se.cockroachdb.order.repository.OrderRepository;

@Component
public class OrderValueProvider implements ValueProvider {
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (Order order : orderRepository.findAll(PageRequest.ofSize(128)
                .withSort(Sort.by("placedAt").descending()))) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if (order.getTags().startsWith(prefix)) {
                result.add(new CompletionProposal(order.getId().toString())
                        .description(order.getTags()));
            }
        }

        return result;
    }
}
