package com.furniro.MessageService.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.database.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    Page<Message> findAllByConversation(Conversation conversation, Pageable pageable);
    Page<Message> findByConversationAndIsInternalFalse(Conversation conversation, Pageable pageable);
    Page<Message> findByConversationAndIsInternalTrue(Conversation conversation, Pageable pageable);
    boolean existsByConversationAndIsReadFalseAndIsInternalFalse(Conversation conversation);

}
