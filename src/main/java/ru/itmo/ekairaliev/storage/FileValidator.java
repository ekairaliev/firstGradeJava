package ru.itmo.ekairaliev.storage;

import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.validation.CustodyEventValidator;
import ru.itmo.ekairaliev.validation.SampleValidator;
import ru.itmo.ekairaliev.validation.SealValidator;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.util.HashSet;
import java.util.Set;

public final class FileValidator {
    public void validate(AppState state) {
        Set<Long> sampleIds = new HashSet<>();
        for (Sample sample : state.getSamples()) {
            SampleValidator.validateEntity(sample);
            if (!sampleIds.add(sample.getId())) {
                throw new ValidationException("Ошибка загрузки: sample id=" + sample.getId() + " повторяется");
            }
        }

        Set<Long> sealIds = new HashSet<>();
        for (Seal seal : state.getSeals()) {
            SealValidator.validateEntity(seal);
            if (!sealIds.add(seal.getId())) {
                throw new ValidationException("Ошибка загрузки: seal id=" + seal.getId() + " повторяется");
            }
            if (!sampleIds.contains(seal.getSampleId())) {
                throw new ValidationException(
                        "Ошибка загрузки: seal.sampleId=" + seal.getSampleId() + " ссылается на несуществующий sample"
                );
            }
        }

        Set<Long> eventIds = new HashSet<>();
        for (CustodyEvent event : state.getCustodyEvents()) {
            CustodyEventValidator.validateEntity(event);
            if (!eventIds.add(event.getId())) {
                throw new ValidationException("Ошибка загрузки: custody_event id=" + event.getId() + " повторяется");
            }
            if (!sampleIds.contains(event.getSampleId())) {
                throw new ValidationException(
                        "Ошибка загрузки: custody_event.sampleId=" + event.getSampleId() + " ссылается на несуществующий sample"
                );
            }
        }
    }
}
