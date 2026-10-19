package com.rtech.mandiplus.repository;

import com.rtech.mandiplus.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.createdAt ASC")
    List<ChatMessage> findChatHistory(@Param("u1") Long u1, @Param("u2") Long u2);

    default List<ChatMessage> findConversationBetweenUsers(Long u1, Long u2) {
        return findChatHistory(u1, u2);
    }

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countTotalUnread(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    Long countUnreadFromSender(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    void markMessagesAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("SELECT DISTINCT m.receiver.id FROM ChatMessage m WHERE m.sender.id = :userId UNION SELECT DISTINCT m.sender.id FROM ChatMessage m WHERE m.receiver.id = :userId")
    List<Long> findDistinctPartnerIds(@Param("userId") Long userId);

    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :u1 AND m.receiver.id = :u2) OR (m.sender.id = :u2 AND m.receiver.id = :u1) ORDER BY m.createdAt DESC")
    List<ChatMessage> findLatestMessageBetween(@Param("u1") Long u1, @Param("u2") Long u2);
}
