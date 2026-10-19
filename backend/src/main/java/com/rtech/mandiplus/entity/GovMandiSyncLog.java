package com.rtech.mandiplus.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gov_mandi_sync_logs")
public class GovMandiSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sync_timestamp", nullable = false)
    private LocalDateTime syncTimestamp;

    @Column(nullable = false, length = 30)
    private String status; // 'SUCCESS', 'FAILED', 'PARTIAL'

    @Column(name = "records_fetched")
    private Integer recordsFetched = 0;

    @Column(name = "records_inserted")
    private Integer recordsInserted = 0;

    @Column(name = "records_updated")
    private Integer recordsUpdated = 0;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "triggered_by", length = 100)
    private String triggeredBy; // 'SCHEDULED_JOB' or admin username

    @Column(name = "duration_ms")
    private Long durationMs;

    public GovMandiSyncLog() {
    }

    public GovMandiSyncLog(String status, Integer recordsFetched, Integer recordsInserted, Integer recordsUpdated,
                           String errorMessage, String triggeredBy, Long durationMs) {
        this.syncTimestamp = LocalDateTime.now();
        this.status = status;
        this.recordsFetched = recordsFetched;
        this.recordsInserted = recordsInserted;
        this.recordsUpdated = recordsUpdated;
        this.errorMessage = errorMessage;
        this.triggeredBy = triggeredBy;
        this.durationMs = durationMs;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getSyncTimestamp() {
        return syncTimestamp;
    }

    public void setSyncTimestamp(LocalDateTime syncTimestamp) {
        this.syncTimestamp = syncTimestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRecordsFetched() {
        return recordsFetched;
    }

    public void setRecordsFetched(Integer recordsFetched) {
        this.recordsFetched = recordsFetched;
    }

    public Integer getRecordsInserted() {
        return recordsInserted;
    }

    public void setRecordsInserted(Integer recordsInserted) {
        this.recordsInserted = recordsInserted;
    }

    public Integer getRecordsUpdated() {
        return recordsUpdated;
    }

    public void setRecordsUpdated(Integer recordsUpdated) {
        this.recordsUpdated = recordsUpdated;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
