package com.furnisight.order.adapter.out.persistence.repository.reservation;

import com.furnisight.order.domain.entities.reservation.StockReservation;
import com.furnisight.order.domain.repository.reservation.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockReservationRepositoryImpl implements StockReservationRepository {

    private final StockReservationJpaRepository jpaRepository;

    @Override
    public StockReservation save(StockReservation reservation) {
        return jpaRepository.save(reservation);
    }

    @Override
    public List<StockReservation> findByOrderCode(String orderCode) {
        return jpaRepository.findByOrderCode(orderCode);
    }

    @Override
    public void deleteByOrderCode(String orderCode) {
        jpaRepository.deleteByOrderCode(orderCode);
    }
}
