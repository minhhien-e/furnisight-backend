package com.furniro.MessageService.database.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furniro.MessageService.database.entity.Conversation;
import com.furniro.MessageService.util.enums.ConversationChannel;
import com.furniro.MessageService.util.enums.ConversationStatus;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    List<Conversation> findByBuyerId(Integer buyerId);
    List<Conversation> findByStaffId(Integer staffId);
    List<Conversation> findByBuyerIdOrStaffId(Integer buyerId, Integer staffId);
    List<Conversation> findByChannel(ConversationChannel channel);
    List<Conversation> findByChannelAndStatus(ConversationChannel channel, ConversationStatus status);
    List<Conversation> findByAssignedAdminId(Integer assignedAdminId);
    Conversation findTopByBuyerIdAndChannelAndStatusNotOrderByUpdatedAtDesc(Integer buyerId, ConversationChannel channel, ConversationStatus status);
    Conversation findTopByBuyerIdAndChannelOrderByUpdatedAtDesc(Integer buyerId, ConversationChannel channel);
}
