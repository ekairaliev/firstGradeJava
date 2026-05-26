package ru.itmo.ekairaliev.validation;

import ru.itmo.ekairaliev.model.CustodyEvent;

import static ru.itmo.ekairaliev.validation.TextRules.loginLikeValue;
import static ru.itmo.ekairaliev.validation.TextRules.maxLen;
import static ru.itmo.ekairaliev.validation.TextRules.norm;
import static ru.itmo.ekairaliev.validation.TextRules.notBlank;

public final class CustodyEventValidator {
    private CustodyEventValidator() {
    }

    public static void validateForCreate(long sampleId, String fromUser, String toUser, String location, String comment) {
        if (sampleId <= 0) {
            throw new ValidationException("Ошибка: sampleId должен быть > 0");
        }
        validatePayload(fromUser, toUser, location, comment);
    }

    public static void validateForUpdate(String fromUser, String toUser, String location, String comment) {
        validatePayload(fromUser, toUser, location, comment);
    }

    public static void validateEntity(CustodyEvent event) {
        if (event == null) {
            throw new ValidationException("Ошибка: custody_event=null");
        }
        if (event.getId() <= 0) {
            throw new ValidationException("Ошибка: id должен быть > 0");
        }

        validateForCreate(
                event.getSampleId(),
                event.getFromUser(),
                event.getToUser(),
                event.getLocation(),
                event.getComment()
        );

        String ownerUsername = notBlank(event.getOwnerUsername(), "owner_username");
        maxLen(ownerUsername, 64, "owner_username");

        if (event.getTransferredAt() == null) {
            throw new ValidationException("Ошибка: transferredAt обязателен");
        }
        if (event.getCreatedAt() == null) {
            throw new ValidationException("Ошибка: createdAt обязателен");
        }
        if (event.getUpdatedAt() == null) {
            throw new ValidationException("Ошибка: updatedAt обязателен");
        }
        if (event.getOwnerId() < 0) {
            throw new ValidationException("Ошибка: ownerId должен быть >= 0");
        }
    }

    private static void validatePayload(String fromUser, String toUser, String location, String comment) {
        fromUser = notBlank(fromUser, "from");
        maxLen(fromUser, 64, "from");
        loginLikeValue(fromUser, "from");

        toUser = notBlank(toUser, "to");
        maxLen(toUser, 64, "to");
        loginLikeValue(toUser, "to");

        location = notBlank(location, "location");
        maxLen(location, 64, "location");

        comment = norm(comment);
        if (comment != null && !comment.isEmpty()) {
            maxLen(comment, 128, "comment");
        }
    }
}
