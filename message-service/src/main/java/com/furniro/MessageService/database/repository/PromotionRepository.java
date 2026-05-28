package com.furniro.MessageService.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniro.MessageService.database.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

}
