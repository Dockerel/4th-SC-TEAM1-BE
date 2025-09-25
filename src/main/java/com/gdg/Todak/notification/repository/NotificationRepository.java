package com.gdg.Todak.notification.repository;

import com.gdg.Todak.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiverUserId(String receiverUserId);
}
