package com.netflix.productivity.outbox.repository;

import com.netflix.productivity.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from OutboxEvent e where e.status = 'PENDING' and e.availableAt <= :now order by e.createdAt asc")
    List<OutboxEvent> findPendingForDispatch(@Param("now") OffsetDateTime now);

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(String status);
}

