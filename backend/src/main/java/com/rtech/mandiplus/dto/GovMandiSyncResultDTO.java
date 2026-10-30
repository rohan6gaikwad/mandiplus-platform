package com.rtech.mandiplus.dto;

import java.time.LocalDateTime;

public class GovMandiSyncResultDTO {

    private String status; // "SUCCESS", "FAILED", "PARTIAL"
    private String message;
    private int recordsFetched;
    private int recordsInserted;
    private int recordsUpdated;
    private long durationMs;
    private LocalDateTime syncTimestamp;

    public GovMandiSyncResultDTO() {
    }

    public GovMandiSyncResultDTO(String status, String message, int recordsFetched, int recordsInserted,
                                int recordsUpdated, long durationMs, LocalDateTime syncTimestamp) {
        this.status = status;
        this.message = message;
        this.recordsFetched = recordsFetched;
        this.recordsInserted = recordsInserted;
        this.recordsUpdated = recordsUpdated;
        this.durationMs = durationMs;
        this.syncTimestamp = syncTimestamp;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getRecordsFetched() { return recordsFetched; }
    public void setRecordsFetched(int recordsFetched) { this.recordsFetched = recordsFetched; }

    public int getRecordsInserted() { return recordsInserted; }
    public void setRecordsInserted(int recordsInserted) { this.recordsInserted = recordsInserted; }

    public int getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(int recordsUpdated) { this.recordsUpdated = recordsUpdated; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getSyncTimestamp() { return syncTimestamp; }
    public void setSyncTimestamp(LocalDateTime syncTimestamp) { this.syncTimestamp = syncTimestamp; }
}
