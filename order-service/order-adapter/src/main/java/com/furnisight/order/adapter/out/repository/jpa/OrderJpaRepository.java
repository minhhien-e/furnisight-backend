package com.furnisight.order.adapter.out.repository.jpa;

import com.furnisight.order.domain.entities.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
}
