package ru.itmo.ekairaliev.validation;

import ru.itmo.ekairaliev.model.Seal;

import static ru.itmo.ekairaliev.validation.TextRules.maxLen;
import static ru.itmo.ekairaliev.validation.TextRules.notBlank;

public final class SealValidator {
    private SealValidator() {
    }

    public static void validateForCreate(long sampleId, String sealNumber, String ownerUsername) {
        if (sampleId <= 0) {
            throw new ValidationException("Ошибка: sampleId должен быть > 0");
        }

        sealNumber = notBlank(sealNumber, "seal_number");
        maxLen(sealNumber, 64, "seal_number");

        ownerUsername = notBlank(ownerUsername, "owner_username");
        maxLen(ownerUsername, 64, "owner_username");
    }

    public static void validateForUpdate(String sealNumber) {
        sealNumber = notBlank(sealNumber, "seal_number");
        maxLen(sealNumber, 64, "seal_number");
    }

    public static void validateEntity(Seal seal) {
        if (seal == null) {
            throw new ValidationException("Ошибка: seal=null");
        }
        if (seal.getId() <= 0) {
            throw new ValidationException("Ошибка: id должен быть > 0");
        }
        if (seal.getSampleId() <= 0) {
            throw new ValidationException("Ошибка: sampleId должен быть > 0");
        }

        String sealNumber = notBlank(seal.getSealNumber(), "seal_number");
        maxLen(sealNumber, 64, "seal_number");

        String ownerUsername = notBlank(seal.getOwnerUsername(), "owner_username");
        maxLen(ownerUsername, 64, "owner_username");

        if (seal.getStatus() == null) {
            throw new ValidationException("Ошибка: status обязателен");
        }
        if (seal.getCreatedAt() == null) {
            throw new ValidationException("Ошибка: createdAt обязателен");
        }
        if (seal.getUpdatedAt() == null) {
            throw new ValidationException("Ошибка: updatedAt обязателен");
        }
        if (seal.getOwnerId() < 0) {
            throw new ValidationException("Ошибка: ownerId должен быть >= 0");
        }
    }
}
