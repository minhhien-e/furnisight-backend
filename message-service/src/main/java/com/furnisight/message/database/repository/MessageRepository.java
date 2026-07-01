package com.furnisight.message.database.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.furnisight.message.database.entity.Conversation;
import com.furnisight.message.database.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    Page<Message> findAllByConversation(Conversation conversation, Pageable pageable);
    Page<Message> findByConversationAndIsInternalFalse(Conversation conversation, Pageable pageable);
    Page<Message> findByConversationAndIsInternalTrue(Conversation conversation, Pageable pageable);
    @Query(
        value = """
            SELECT *
            FROM messages m
            WHERE m.conversation_id = :conversationId
              AND (:includeInternal = true OR COALESCE(m.isinternal, false) = false)
              AND (
                LOWER(COALESCE(m.content, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.attachmentname, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.attachmenttype, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.attachmenturl, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.mediaid, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(CAST(m.attachments AS text), '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY m.createdat ASC, m.id ASC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM messages m
            WHERE m.conversation_id = :conversationId
              AND (:includeInternal = true OR COALESCE(m.isinternal, false) = false)
              AND (
                LOWER(COALESCE(m.content, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.attachmentname, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.attachmenttype, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.attachmenturl, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(m.mediaid, '')) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(COALESCE(CAST(m.attachments AS text), '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            """,
        nativeQuery = true
    )
    Page<Message> searchByConversation(
        @Param("conversationId") Integer conversationId,
        @Param("query") String query,
        @Param("includeInternal") boolean includeInternal,
        Pageable pageable);
    boolean existsByConversationAndIsReadFalseAndIsInternalFalse(Conversation conversation);

}
