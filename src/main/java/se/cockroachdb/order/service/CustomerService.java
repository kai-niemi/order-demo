package se.cockroachdb.order.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import se.cockroachdb.order.annotation.Remark;
import se.cockroachdb.order.annotation.TransactionSupports;
import se.cockroachdb.order.domain.Customer;
import se.cockroachdb.order.repository.CustomerRepository;

@Service
@TransactionSupports
public class CustomerService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerRepository customerRepository;

    @Remark("This is highly inefficient (full scan), equivalent to 'order by random()'")
    public List<Customer> findRandomCustomers(int count) {
        long qty = customerRepository.count();
        int offset = ThreadLocalRandom.current().nextInt(Math.max(1, (int) qty - count)) / count;
        return customerRepository.findAll(
                PageRequest.of(Math.max(0, offset), count)).getContent();
    }
}
