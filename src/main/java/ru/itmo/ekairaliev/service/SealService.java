package ru.itmo.ekairaliev.service;

import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;
import ru.itmo.ekairaliev.repository.SealRepository;
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
    private SealRepository sealRepository;

    private final SampleService sampleService;

    public SealService(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    public void bindRepository(SealRepository sealRepository) {
        this.sealRepository = sealRepository;
        replaceAll(sealRepository.findAll());
    }

    public Seal add(long sampleId, String sealNumber, String ownerUsername) {
        return add(sampleId, sealNumber, ownerUsername, 0);
    }

    public Seal add(long sampleId, String sealNumber, String ownerUsername, long ownerId) {
        SealValidator.validateForCreate(sampleId, sealNumber, ownerUsername);
        sampleService.getById(sampleId);
        sampleService.ensureOwner(sampleId, ownerId);

        Instant now = Instant.now();
        Seal seal = sealRepository == null
                ? new Seal(nextId++, sampleId, SealStatus.ACTIVE, sealNumber.trim(), ownerUsername.trim(), now, now, ownerId)
                : sealRepository.insert(sampleId, SealStatus.ACTIVE, sealNumber.trim(), ownerUsername.trim(), now, now, ownerId);

        SealValidator.validateEntity(seal);
        seals.put(seal.getId(), seal);
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
        return update(id, sealNumber, 0);
    }

    public Seal update(long id, String sealNumber, long actorId) {
        validateId(id);
        SealValidator.validateForUpdate(sealNumber);

        Seal seal = getById(id);
        ensureOwner(seal, actorId);
        if (seal.getStatus() == SealStatus.BROKEN) {
            throw new ValidationException("Ошибка: broken seal с id=" + id + " нельзя изменять");
        }

        seal.setSealNumber(sealNumber.trim());
        seal.touch();

        SealValidator.validateEntity(seal);
        if (sealRepository != null) {
            sealRepository.update(seal);
        }
        return seal;
    }

    public Seal remove(long id) {
        return remove(id, 0);
    }

    public Seal remove(long id, long actorId) {
        validateId(id);

        Seal seal = getById(id);
        ensureOwner(seal, actorId);
        if (sealRepository != null) {
            sealRepository.delete(id);
        }
        seals.remove(id);
        return seal;
    }

    public void breakSeal(long id) {
        breakSeal(id, 0);
    }

    public void breakSeal(long id, long actorId) {
        validateId(id);

        Seal seal = getById(id);
        ensureOwner(seal, actorId);
        if (seal.getStatus() == SealStatus.BROKEN) {
            throw new ValidationException("Ошибка: пломба уже BROKEN");
        }

        seal.setStatus(SealStatus.BROKEN);
        seal.touch();
        SealValidator.validateEntity(seal);
        if (sealRepository != null) {
            sealRepository.update(seal);
        }
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

    public boolean canModify(Seal seal, long actorId) {
        return actorId <= 0 || seal.getOwnerId() == 0 || seal.getOwnerId() == actorId;
    }

    private void ensureOwner(Seal seal, long actorId) {
        if (!canModify(seal, actorId)) {
            throw new ValidationException("Ошибка: у вас нет прав на изменение этого объекта");
        }
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
