package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.model.CustodyEvent;

import java.time.Instant;
import java.util.List;

public interface CustodyEventRepository {
    List<CustodyEvent> findAll();

    CustodyEvent insert(long sampleId, String fromUser, String toUser, String location, String comment,
                        Instant transferredAt, String ownerUsername, Instant createdAt, Instant updatedAt, long ownerId);

    void update(CustodyEvent event);

    void delete(long id);
}
