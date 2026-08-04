package org.admin.npapplication.repository;

import org.admin.npapplication.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrueAndFeaturedTrue(Pageable pageable);

    Page<Product> findByActiveTrueAndCategory(String category, Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description, Pageable pageable);
}
