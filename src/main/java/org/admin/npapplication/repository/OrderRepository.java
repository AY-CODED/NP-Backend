package org.admin.npapplication.repository;

import org.admin.npapplication.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.id = :orderId")
    Optional<Order> findByUserIdAndId(Long userId, Long orderId);

    List<Order> findByPaymentReference(String paymentReference);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.paymentStatus = :paymentStatus")
    BigDecimal sumTotalByPaymentStatus(PaymentStatus paymentStatus);

    List<Order> findTop5ByOrderByCreatedAtDesc();
}