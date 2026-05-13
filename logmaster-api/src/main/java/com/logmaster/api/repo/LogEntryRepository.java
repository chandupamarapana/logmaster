package com.logmaster.api.repo;

import com.logmaster.api.model.LogEntry;
import com.logmaster.api.model.LogType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    List<LogEntry> findByDeliveryIdOrderByLogTypeAscEntryOrderAsc(Long deliveryId);

    List<LogEntry> findByDeliveryIdAndLogTypeOrderByEntryOrderAsc(Long deliveryId, LogType logType);

    int countByDeliveryIdAndLogType(Long deliveryId, LogType logType);
}

