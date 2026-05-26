package com.furnisight.order.adapter.out.persistence.repository.reservation;

import com.furnisight.order.domain.entities.reservation.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationJpaRepository extends JpaRepository<StockReservation, UUID> {
    List<StockReservation> findByOrderCode(String orderCode);
    void deleteByOrderCode(String orderCode);
}
