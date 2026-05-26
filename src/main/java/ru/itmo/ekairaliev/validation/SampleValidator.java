package ru.itmo.ekairaliev.validation;

import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;

import static ru.itmo.ekairaliev.validation.TextRules.maxLen;
import static ru.itmo.ekairaliev.validation.TextRules.notBlank;

public final class SampleValidator {
    private SampleValidator() {
    }

    public static void validateForCreate(String name) {
        name = notBlank(name, "name");
        maxLen(name, 64, "name");
    }

    public static void validateForUpdate(String name) {
        validateForCreate(name);
    }

    public static void validateEntity(Sample sample) {
        if (sample == null) {
            throw new ValidationException("Ошибка: sample=null");
        }
        if (sample.getId() <= 0) {
            throw new ValidationException("Ошибка: id должен быть > 0");
        }

        String name = notBlank(sample.getName(), "name");
        maxLen(name, 64, "name");

        if (sample.getHoldStatus() == null) {
            throw new ValidationException("Ошибка: holdStatus обязателен");
        }
        if (sample.getHoldStatus() != SampleHoldStatus.ACTIVE && sample.getHoldStatus() != SampleHoldStatus.ON_HOLD) {
            throw new ValidationException("Ошибка: неверный holdStatus");
        }

        if (sample.getCreatedAt() == null) {
            throw new ValidationException("Ошибка: createdAt обязателен");
        }
        if (sample.getUpdatedAt() == null) {
            throw new ValidationException("Ошибка: updatedAt обязателен");
        }
        if (sample.getOwnerId() < 0) {
            throw new ValidationException("Ошибка: ownerId должен быть >= 0");
        }
    }
}
