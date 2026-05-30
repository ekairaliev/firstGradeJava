package ru.itmo.ekairaliev.service;

import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.repository.CustodyEventRepository;
import ru.itmo.ekairaliev.validation.CustodyEventValidator;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CustodyService {
    private static final Comparator<CustodyEvent> REVERSED_EVENT_ORDER =
            Comparator.comparing(CustodyEvent::getTransferredAt)
                    .thenComparingLong(CustodyEvent::getId)
                    .reversed();

    private static final Comparator<CustodyEvent> CHRONOLOGICAL_EVENT_ORDER =
            Comparator.comparing(CustodyEvent::getTransferredAt)
                    .thenComparingLong(CustodyEvent::getId);

    private final Map<Long, CustodyEvent> events = new LinkedHashMap<>();
    private long nextId = 1;
    private CustodyEventRepository custodyEventRepository;

    private final SampleService sampleService;

    public CustodyService(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    public void bindRepository(CustodyEventRepository custodyEventRepository) {
        this.custodyEventRepository = custodyEventRepository;
        replaceAll(custodyEventRepository.findAll());
    }

    public CustodyEvent add(long sampleId, String fromUser, String toUser, String location, String comment, String ownerUsername) {
        return add(sampleId, fromUser, toUser, location, comment, ownerUsername, 0);
    }

    public CustodyEvent add(long sampleId, String fromUser, String toUser, String location, String comment, String ownerUsername, long ownerId) {
        CustodyEventValidator.validateForCreate(sampleId, fromUser, toUser, location, comment);

        Sample sample = sampleService.getById(sampleId);
        sampleService.ensureOwner(sampleId, ownerId);
        if (sample.getHoldStatus() == SampleHoldStatus.ON_HOLD) {
            throw new ValidationException("Ошибка: sample с id=" + sampleId + " находится ON_HOLD, сначала выполните sample_release");
        }

        Instant now = Instant.now();
        String normalizedOwner = ownerUsername == null || ownerUsername.trim().isEmpty() ? "SYSTEM" : ownerUsername.trim();
        CustodyEvent event = custodyEventRepository == null
                ? new CustodyEvent(
                nextId++,
                sampleId,
                fromUser.trim(),
                toUser.trim(),
                location.trim(),
                comment == null ? null : comment.trim(),
                now,
                normalizedOwner,
                now,
                now,
                ownerId
        )
                : custodyEventRepository.insert(
                sampleId,
                fromUser.trim(),
                toUser.trim(),
                location.trim(),
                comment == null ? null : comment.trim(),
                now,
                normalizedOwner,
                now,
                now,
                ownerId
        );

        CustodyEventValidator.validateEntity(event);
        events.put(event.getId(), event);
        return event;
    }

    public CustodyEvent getById(long id) {
        validateId(id);

        CustodyEvent event = events.get(id);
        if (event == null) {
            throw new ValidationException("Ошибка: custody_event с id=" + id + " не найден");
        }
        return event;
    }

    public List<CustodyEvent> list() {
        return getAll();
    }

    public List<CustodyEvent> getAll() {
        return events.values().stream()
                .sorted(REVERSED_EVENT_ORDER)
                .collect(Collectors.toList());
    }

    public void replaceAll(List<CustodyEvent> newEvents) {
        events.clear();
        long maxId = 0;
        for (CustodyEvent event : newEvents.stream().sorted(CHRONOLOGICAL_EVENT_ORDER).toList()) {
            events.put(event.getId(), event);
            maxId = Math.max(maxId, event.getId());
        }
        nextId = maxId + 1;
    }

    public CustodyEvent update(long id, String fromUser, String toUser, String location, String comment) {
        return update(id, fromUser, toUser, location, comment, 0);
    }

    public CustodyEvent update(long id, String fromUser, String toUser, String location, String comment, long actorId) {
        validateId(id);
        CustodyEventValidator.validateForUpdate(fromUser, toUser, location, comment);

        CustodyEvent event = getById(id);
        ensureOwner(event, actorId);
        ensureLastEvent(event);

        event.setFromUser(fromUser.trim());
        event.setToUser(toUser.trim());
        event.setLocation(location.trim());
        event.setComment(comment == null || comment.trim().isEmpty() ? null : comment.trim());
        event.touch();

        CustodyEventValidator.validateEntity(event);
        if (custodyEventRepository != null) {
            custodyEventRepository.update(event);
        }
        return event;
    }

    public CustodyEvent remove(long id) {
        return remove(id, 0);
    }

    public CustodyEvent remove(long id, long actorId) {
        validateId(id);

        CustodyEvent event = getById(id);
        ensureOwner(event, actorId);
        ensureLastEvent(event);
        if (custodyEventRepository != null) {
            custodyEventRepository.delete(id);
        }
        events.remove(id);
        return event;
    }

    public List<CustodyEvent> listBySample(long sampleId) {
        validateSampleId(sampleId);
        sampleService.getById(sampleId);

        return events.values().stream()
                .filter(event -> event.getSampleId() == sampleId)
                .sorted(REVERSED_EVENT_ORDER)
                .collect(Collectors.toList());
    }

    public List<CustodyEvent> listBySampleLastN(long sampleId, int n) {
        if (n <= 0) {
            throw new ValidationException("Ошибка: N должно быть > 0");
        }

        return listBySample(sampleId).stream()
                .limit(n)
                .collect(Collectors.toList());
    }

    public Optional<String> currentOwner(long sampleId) {
        List<CustodyEvent> list = listBySample(sampleId);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(0).getToUser());
    }

    public List<CustodyEvent> listBySampleChronological(long sampleId) {
        return listBySample(sampleId).stream()
                .sorted(CHRONOLOGICAL_EVENT_ORDER)
                .collect(Collectors.toList());
    }

    public boolean hasAnyBySample(long sampleId) {
        validateSampleId(sampleId);

        return events.values().stream()
                .anyMatch(event -> event.getSampleId() == sampleId);
    }

    private void ensureLastEvent(CustodyEvent event) {
        List<CustodyEvent> sampleEvents = listBySample(event.getSampleId());
        if (!sampleEvents.isEmpty() && sampleEvents.get(0).getId() != event.getId()) {
            throw new ValidationException("Ошибка: можно изменять или удалять только последнее custody_event для sample id=" + event.getSampleId());
        }
    }

    public boolean canModify(CustodyEvent event, long actorId) {
        return actorId <= 0 || event.getOwnerId() == 0 || event.getOwnerId() == actorId;
    }

    private void ensureOwner(CustodyEvent event, long actorId) {
        if (!canModify(event, actorId)) {
            throw new ValidationException("Ошибка: у вас нет прав на изменение этого объекта");
        }
    }

    private void validateId(long id) {
        if (id <= 0) {
            throw new ValidationException("Ошибка: event_id должен быть > 0");
        }
    }

    private void validateSampleId(long sampleId) {
        if (sampleId <= 0) {
            throw new ValidationException("Ошибка: sample_id должен быть > 0");
        }
    }
}
