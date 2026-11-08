package com.roomsetu.repository;

import com.roomsetu.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByMatchIdOrderByDueDateAsc(Long matchId);

    // Used by the (stubbed) notification job — see ReminderService note at the bottom
    List<Reminder> findByDueDateAndAcknowledgedFalse(LocalDate dueDate);
}