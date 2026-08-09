package com.furnisight.message.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furnisight.message.database.entity.MessageTemplate;

@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Integer> {
}
