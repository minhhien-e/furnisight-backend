package com.furnisight.order.domain.repository.reservation;

import com.furnisight.order.domain.entities.reservation.StockReservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {
    StockReservation save(StockReservation reservation);
    List<StockReservation> findByOrderCode(String orderCode);
    void deleteByOrderCode(String orderCode);
}
