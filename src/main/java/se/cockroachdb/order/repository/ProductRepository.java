package se.cockroachdb.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.cockroachdb.order.domain.Product;

@Repository
public interface ProductRepository extends CrudRepository<Product, UUID>, PagingAndSortingRepository<Product, UUID> {
    @Query("select * from product p where p.id=:id FOR UPDATE")
    Optional<Product> findProductByIdForUpdate(@Param("id") UUID id);

    @Query("select * from product p where p.id=:id")
    Optional<Product> findProductById(@Param("id") UUID id);
}
