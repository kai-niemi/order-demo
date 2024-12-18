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

import se.cockroachdb.order.domain.Customer;
import se.cockroachdb.order.repository.CustomerRepository;

@Component
public class CustomerValueProvider implements ValueProvider {
    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();

        for (Customer customer : customerRepository.findAll(PageRequest.ofSize(128)
                .withSort(Sort.by("userName").descending()))) {
            String prefix = completionContext.currentWordUpToCursor();
            if (prefix == null) {
                prefix = "";
            }
            if (customer.getUserName().startsWith(prefix)) {
                result.add(new CompletionProposal(customer.getId().toString())
                        .description(customer.getUserName()));
            }
        }

        return result;
    }
}
