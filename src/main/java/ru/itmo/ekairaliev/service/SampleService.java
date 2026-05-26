package ru.itmo.ekairaliev.service;

import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.repository.SampleRepository;
import ru.itmo.ekairaliev.validation.SampleValidator;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SampleService {

    private final Map<Long, Sample> samples = new LinkedHashMap<>();
    private long nextId = 1;
    private SampleRepository sampleRepository;

    private SealService sealService;
    private CustodyService custodyService;

    public void bindRelations(SealService sealService, CustodyService custodyService) {
        this.sealService = sealService;
        this.custodyService = custodyService;
    }

    public void bindRepository(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
        replaceAll(sampleRepository.findAll());
    }

    public Sample add(String name) {
        return add(name, 0);
    }

    public Sample add(String name, long ownerId) {
        SampleValidator.validateForCreate(name);

        Instant now = Instant.now();
        Sample sample = sampleRepository == null
                ? new Sample(nextId++, name.trim(), SampleHoldStatus.ACTIVE, now, now, ownerId)
                : sampleRepository.insert(name.trim(), SampleHoldStatus.ACTIVE, now, now, ownerId);

        SampleValidator.validateEntity(sample);
        samples.put(sample.getId(), sample);
        return sample;
    }

    public Sample getById(long id) {
        validateId(id);

        Sample sample = samples.get(id);
        if (sample == null) {
            throw new ValidationException("Ошибка: sample с id=" + id + " не найден");
        }
        return sample;
    }

    public List<Sample> list() {
        return getAll();
    }

    public List<Sample> getAll() {
        return new ArrayList<>(samples.values());
    }

    public void replaceAll(List<Sample> newSamples) {
        samples.clear();
        long maxId = 0;
        for (Sample sample : newSamples.stream().sorted(Comparator.comparingLong(Sample::getId)).toList()) {
            samples.put(sample.getId(), sample);
            maxId = Math.max(maxId, sample.getId());
        }
        nextId = maxId + 1;
    }

    public Sample update(long id, String name) {
        return update(id, name, 0);
    }

    public Sample update(long id, String name, long actorId) {
        validateId(id);
        SampleValidator.validateForUpdate(name);

        Sample sample = getById(id);
        ensureOwner(sample, actorId);
        sample.setName(name.trim());
        sample.touch();

        SampleValidator.validateEntity(sample);
        if (sampleRepository != null) {
            sampleRepository.update(sample);
        }
        return sample;
    }

    public Sample remove(long id) {
        return remove(id, 0);
    }

    public Sample remove(long id, long actorId) {
        validateId(id);
        Sample sample = getById(id);
        ensureOwner(sample, actorId);

        if (sealService != null && sealService.hasAnyBySample(id)) {
            throw new ValidationException("Ошибка: нельзя удалить sample с id=" + id + ", пока у него есть связанные seal");
        }
        if (custodyService != null && custodyService.hasAnyBySample(id)) {
            throw new ValidationException("Ошибка: нельзя удалить sample с id=" + id + ", пока у него есть связанные custody_event");
        }

        if (sampleRepository != null) {
            sampleRepository.delete(id);
        }
        samples.remove(id);
        return sample;
    }

    public void hold(long id) {
        hold(id, 0);
    }

    public void hold(long id, long actorId) {
        validateId(id);

        Sample sample = getById(id);
        ensureOwner(sample, actorId);
        if (sample.getHoldStatus() == SampleHoldStatus.ON_HOLD) {
            throw new ValidationException("Ошибка: sample с id=" + id + " уже ON_HOLD");
        }

        updateHoldStatus(sample, SampleHoldStatus.ON_HOLD);
    }

    public void release(long id) {
        release(id, 0);
    }

    public void release(long id, long actorId) {
        validateId(id);

        Sample sample = getById(id);
        ensureOwner(sample, actorId);
        if (sample.getHoldStatus() == SampleHoldStatus.ACTIVE) {
            throw new ValidationException("Ошибка: sample с id=" + id + " уже ACTIVE");
        }

        updateHoldStatus(sample, SampleHoldStatus.ACTIVE);
    }

    private void updateHoldStatus(Sample sample, SampleHoldStatus holdStatus) {
        sample.setHoldStatus(holdStatus);
        sample.touch();

        SampleValidator.validateEntity(sample);
        if (sampleRepository != null) {
            sampleRepository.update(sample);
        }
    }

    public boolean canModify(Sample sample, long actorId) {
        return actorId <= 0 || sample.getOwnerId() == 0 || sample.getOwnerId() == actorId;
    }

    public void ensureOwner(long sampleId, long actorId) {
        ensureOwner(getById(sampleId), actorId);
    }

    private void ensureOwner(Sample sample, long actorId) {
        if (!canModify(sample, actorId)) {
            throw new ValidationException("Ошибка: у вас нет прав на изменение этого объекта");
        }
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new ValidationException("Ошибка: sample_id должен быть > 0");
        }
    }
}
