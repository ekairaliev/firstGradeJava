package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;

import java.time.Instant;
import java.util.List;

public interface SealRepository {
    List<Seal> findAll();

    Seal insert(long sampleId, SealStatus status, String sealNumber, String ownerUsername,
                Instant createdAt, Instant updatedAt, long ownerId);

    void update(Seal seal);

    void delete(long id);
}
