package com.furnisight.message.database.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furnisight.message.database.entity.Conversation;
import com.furnisight.message.util.enums.ConversationChannel;
import com.furnisight.message.util.enums.ConversationStatus;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    List<Conversation> findByBuyerId(Integer buyerId);
    List<Conversation> findByStaffId(Integer staffId);
    List<Conversation> findByBuyerIdOrStaffId(Integer buyerId, Integer staffId);
    List<Conversation> findByChannel(ConversationChannel channel);
    List<Conversation> findByChannelAndStatus(ConversationChannel channel, ConversationStatus status);
    List<Conversation> findByAssignedAdminId(Integer assignedAdminId);
    Conversation findTopByBuyerIdAndChannelAndStatusNotOrderByUpdatedAtDesc(Integer buyerId, ConversationChannel channel, ConversationStatus status);
    Conversation findTopByBuyerIdOrderByUpdatedAtDesc(Integer buyerId);
    Conversation findTopByBuyerIdAndChannelOrderByUpdatedAtDesc(Integer buyerId, ConversationChannel channel);
}
