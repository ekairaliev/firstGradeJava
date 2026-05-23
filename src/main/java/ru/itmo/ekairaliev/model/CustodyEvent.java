package ru.itmo.ekairaliev.model;

import java.time.Instant;
import java.util.Objects;

public final class CustodyEvent {
    private final long id;
    private final long sampleId;
    private String fromUser;
    private String toUser;
    private String location;
    private String comment;
    private final Instant transferredAt;
    private final String ownerUsername;
    private final Instant createdAt;
    private Instant updatedAt;

    public CustodyEvent(long id, long sampleId, String fromUser, String toUser, String location,
                        String comment, Instant transferredAt, String ownerUsername, Instant createdAt,
                        Instant updatedAt) {
        this.id = id;
        this.sampleId = sampleId;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.location = location;
        this.comment = comment;
        this.transferredAt = transferredAt;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public long getSampleId() {
        return sampleId;
    }

    public String getFromUser() {
        return fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public Instant getTransferredAt() {
        return transferredAt;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustodyEvent that = (CustodyEvent) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CustodyEvent{" +
                "id=" + id +
                ", sampleId=" + sampleId +
                ", fromUser='" + fromUser + '\'' +
                ", toUser='" + toUser + '\'' +
                ", location='" + location + '\'' +
                ", comment='" + comment + '\'' +
                ", transferredAt=" + transferredAt +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
