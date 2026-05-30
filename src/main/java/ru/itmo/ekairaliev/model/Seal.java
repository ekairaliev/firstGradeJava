package ru.itmo.ekairaliev.model;

import java.time.Instant;
import java.util.Objects;

public final class Seal {
    private final long id;
    private final long sampleId;
    private String sealNumber;
    private SealStatus status;
    private final String ownerUsername;
    private final Instant createdAt;
    private Instant updatedAt;
    private final long ownerId;

    public Seal(long id, long sampleId, SealStatus status, String sealNumber, String ownerUsername,
                Instant createdAt, Instant updatedAt, long ownerId) {
        this.id = id;
        this.sampleId = sampleId;
        this.status = status;
        this.sealNumber = sealNumber;
        this.ownerUsername = ownerUsername;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ownerId = ownerId;
    }

    public long getId() {
        return id;
    }

    public long getSampleId() {
        return sampleId;
    }

    public String getSealNumber() {
        return sealNumber;
    }

    public SealStatus getStatus() {
        return status;
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

    public long getOwnerId() {
        return ownerId;
    }

    public void setSealNumber(String sealNumber) {
        this.sealNumber = sealNumber;
    }

    public void setStatus(SealStatus status) {
        this.status = status;
    }

    // Помечаем, что объект был изменен.
    public void touch() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seal seal = (Seal) o;
        return id == seal.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Seal{" +
                "id=" + id +
                ", sampleId=" + sampleId +
                ", sealNumber='" + sealNumber + '\'' +
                ", status=" + status +
                ", ownerUsername='" + ownerUsername + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", ownerId=" + ownerId +
                '}';
    }
}
