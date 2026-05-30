package ru.itmo.ekairaliev.model;

import java.time.Instant;
import java.util.Objects;

public final class Sample {
    private final long id;                 // назначается программой
    private String name;                   // до 64, не пусто
    private SampleHoldStatus holdStatus;   // ACTIVE/ON_HOLD
    private final Instant createdAt;       // назначается программой
    private Instant updatedAt;             // обновляется при изменениях
    private final long ownerId;            // id пользователя-владельца

    //конструктор
    public Sample(long id, String name, SampleHoldStatus holdStatus, Instant createdAt, Instant updatedAt, long ownerId) {
        this.id = id;
        this.name = name;
        this.holdStatus = holdStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.ownerId = ownerId;
    }
    //геттеры
    public long getId() { return id; }
    public String getName() { return name; }
    public SampleHoldStatus getHoldStatus() { return holdStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getOwnerId() { return ownerId; }
    //сеттеры
    public void setName(String name) { this.name = name; }
    public void setHoldStatus(SampleHoldStatus holdStatus) { this.holdStatus = holdStatus; }

    public void touch() { this.updatedAt = Instant.now(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sample sample = (Sample) o;
        return id == sample.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", holdStatus=" + holdStatus +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", ownerId=" + ownerId +
                '}';
    }
}
