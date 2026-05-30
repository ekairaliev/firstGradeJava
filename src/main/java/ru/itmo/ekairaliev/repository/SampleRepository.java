package ru.itmo.ekairaliev.repository;

import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;

import java.time.Instant;
import java.util.List;

public interface SampleRepository {
    List<Sample> findAll();

    Sample insert(String name, SampleHoldStatus holdStatus, Instant createdAt, Instant updatedAt, long ownerId);

    void update(Sample sample);

    void delete(long id);
}
