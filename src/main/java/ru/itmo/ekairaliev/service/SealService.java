package ru.itmo.ekairaliev.service;

import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;
import ru.itmo.ekairaliev.validation.SealValidator;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SealService {
    private final Map<Long, Seal> seals = new LinkedHashMap<>();
    private long nextId = 1;

    private final SampleService sampleService;

    public SealService(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    public Seal add(long sampleId, String sealNumber, String ownerUsername) {
        SealValidator.validateForCreate(sampleId, sealNumber, ownerUsername);
        sampleService.getById(sampleId);

        long id = nextId++;
        Instant now = Instant.now();

        Seal seal = new Seal(
                id,
                sampleId,
                SealStatus.ACTIVE,
                sealNumber.trim(),
                ownerUsername.trim(),
                now,
                now
        );

        SealValidator.validateEntity(seal);
        seals.put(id, seal);
        return seal;
    }

    public Seal getById(long id) {
        validateId(id);

        Seal seal = seals.get(id);
        if (seal == null) {
            throw new ValidationException("Ошибка: seal с id=" + id + " не найден");
        }
        return seal;
    }

    public List<Seal> list() {
        return getAll();
    }

    public List<Seal> getAll() {
        return new ArrayList<>(seals.values());
    }

    public void replaceAll(List<Seal> newSeals) {
        seals.clear();
        long maxId = 0;
        for (Seal seal : newSeals.stream().sorted(Comparator.comparingLong(Seal::getId)).toList()) {
            seals.put(seal.getId(), seal);
            maxId = Math.max(maxId, seal.getId());
        }
        nextId = maxId + 1;
    }

    public Seal update(long id, String sealNumber) {
        validateId(id);
        SealValidator.validateForUpdate(sealNumber);

        Seal seal = getById(id);
        if (seal.getStatus() == SealStatus.BROKEN) {
            throw new ValidationException("Ошибка: broken seal с id=" + id + " нельзя изменять");
        }

        seal.setSealNumber(sealNumber.trim());
        seal.touch();

        SealValidator.validateEntity(seal);
        return seal;
    }

    public Seal remove(long id) {
        validateId(id);

        Seal seal = getById(id);
        seals.remove(id);
        return seal;
    }

    public void breakSeal(long id) {
        validateId(id);

        Seal seal = getById(id);
        if (seal.getStatus() == SealStatus.BROKEN) {
            throw new ValidationException("Ошибка: пломба уже BROKEN");
        }

        seal.setStatus(SealStatus.BROKEN);
        seal.touch();
        SealValidator.validateEntity(seal);
    }

    public boolean hasAnyBySample(long sampleId) {
        validateSampleId(sampleId);

        for (Seal seal : seals.values()) {
            if (seal.getSampleId() == sampleId) {
                return true;
            }
        }
        return false;
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new ValidationException("Ошибка: seal_id должен быть > 0");
        }
    }

    private void validateSampleId(long sampleId) {
        if (sampleId <= 0) {
            throw new ValidationException("Ошибка: sample_id должен быть > 0");
        }
    }
}
