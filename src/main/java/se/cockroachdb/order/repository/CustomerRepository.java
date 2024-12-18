package se.cockroachdb.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import se.cockroachdb.order.domain.Customer;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID>,
        PagingAndSortingRepository<Customer, UUID> {
    Optional<Customer> findByUserName(String userName);
}
