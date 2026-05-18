package ru.itmo.ekairaliev.service;

import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
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

    private SealService sealService;
    private CustodyService custodyService;

    public void bindRelations(SealService sealService, CustodyService custodyService) {
        this.sealService = sealService;
        this.custodyService = custodyService;
    }

    public Sample add(String name) {
        SampleValidator.validateForCreate(name);

        long id = nextId++;
        Instant now = Instant.now();

        Sample sample = new Sample(
                id,
                name.trim(),
                SampleHoldStatus.ACTIVE,
                now,
                now
        );

        SampleValidator.validateEntity(sample);
        samples.put(id, sample);
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
        validateId(id);
        SampleValidator.validateForUpdate(name);

        Sample sample = getById(id);
        sample.setName(name.trim());
        sample.touch();

        SampleValidator.validateEntity(sample);
        return sample;
    }

    public Sample remove(long id) {
        validateId(id);
        Sample sample = getById(id);

        if (sealService != null && sealService.hasAnyBySample(id)) {
            throw new ValidationException("Ошибка: нельзя удалить sample с id=" + id + ", пока у него есть связанные seal");
        }
        if (custodyService != null && custodyService.hasAnyBySample(id)) {
            throw new ValidationException("Ошибка: нельзя удалить sample с id=" + id + ", пока у него есть связанные custody_event");
        }

        samples.remove(id);
        return sample;
    }

    public void hold(long id) {
        validateId(id);

        Sample sample = getById(id);
        if (sample.getHoldStatus() == SampleHoldStatus.ON_HOLD) {
            throw new ValidationException("Ошибка: sample с id=" + id + " уже ON_HOLD");
        }

        updateHoldStatus(sample, SampleHoldStatus.ON_HOLD);
    }

    public void release(long id) {
        validateId(id);

        Sample sample = getById(id);
        if (sample.getHoldStatus() == SampleHoldStatus.ACTIVE) {
            throw new ValidationException("Ошибка: sample с id=" + id + " уже ACTIVE");
        }

        updateHoldStatus(sample, SampleHoldStatus.ACTIVE);
    }

    private void updateHoldStatus(Sample sample, SampleHoldStatus holdStatus) {
        sample.setHoldStatus(holdStatus);
        sample.touch();

        SampleValidator.validateEntity(sample);
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new ValidationException("Ошибка: sample_id должен быть > 0");
        }
    }
}
