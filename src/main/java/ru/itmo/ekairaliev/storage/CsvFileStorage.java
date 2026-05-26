package ru.itmo.ekairaliev.storage;

import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvFileStorage {
    private static final int LEGACY_COLUMN_COUNT = 15;
    private static final int COLUMN_COUNT = 16;
    private static final String LEGACY_HEADER =
            "type,id,sampleId,name,holdStatus,createdAt,updatedAt,sealNumber,status,ownerUsername,fromUser,toUser,location,comment,transferredAt";
    private static final String HEADER =
            "type,id,sampleId,name,holdStatus,createdAt,updatedAt,sealNumber,status,ownerUsername,fromUser,toUser,location,comment,transferredAt,ownerId";

    public void save(Path path, AppState state) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);

        for (Sample sample : state.getSamples()) {
            lines.add(CsvSupport.toLine(sampleColumns(sample)));
        }

        for (Seal seal : state.getSeals()) {
            lines.add(CsvSupport.toLine(sealColumns(seal)));
        }

        for (CustodyEvent event : state.getCustodyEvents()) {
            lines.add(CsvSupport.toLine(custodyColumns(event)));
        }

        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ValidationException("Ошибка сохранения: " + e.getMessage());
        }
    }

    public AppState load(Path path) {
        List<String> lines = readAllLines(path);

        if (lines.isEmpty()) {
            throw new ValidationException("Ошибка загрузки: файл пустой");
        }
        boolean legacyFormat = LEGACY_HEADER.equals(lines.get(0));
        if (!HEADER.equals(lines.get(0)) && !legacyFormat) {
            throw new ValidationException("Ошибка загрузки: некорректный заголовок CSV");
        }

        List<Sample> samples = new ArrayList<>();
        List<Seal> seals = new ArrayList<>();
        List<CustodyEvent> custodyEvents = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }

            List<String> columns = CsvSupport.parseLine(line, i + 1, legacyFormat ? LEGACY_COLUMN_COUNT : COLUMN_COUNT);
            String type = columns.get(0);
            switch (type) {
                case "SAMPLE" -> samples.add(parseSample(columns, i + 1));
                case "SEAL" -> seals.add(parseSeal(columns, i + 1));
                case "CUSTODY" -> custodyEvents.add(parseCustodyEvent(columns, i + 1));
                default -> throw new ValidationException("Ошибка загрузки: неизвестный тип строки '" + type + "' в строке " + (i + 1));
            }
        }

        return new AppState(samples, seals, custodyEvents);
    }

    private List<String> readAllLines(Path path) {
        try {
            if (!Files.exists(path)) {
                throw new ValidationException("Ошибка загрузки: файл не существует");
            }
            if (!Files.isReadable(path)) {
                throw new ValidationException("Ошибка загрузки: файл недоступен для чтения");
            }
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ValidationException("Ошибка загрузки: " + e.getMessage());
        }
    }

    private Sample parseSample(List<String> columns, int lineNumber) {
        return new Sample(
                CsvSupport.parseLong(columns.get(1), "id", lineNumber),
                columns.get(3),
                CsvSupport.parseEnum(columns.get(4), SampleHoldStatus.class, "holdStatus", lineNumber),
                CsvSupport.parseInstant(columns.get(5), "createdAt", lineNumber),
                CsvSupport.parseInstant(columns.get(6), "updatedAt", lineNumber),
                parseOwnerId(columns, lineNumber)
        );
    }

    private Seal parseSeal(List<String> columns, int lineNumber) {
        return new Seal(
                CsvSupport.parseLong(columns.get(1), "id", lineNumber),
                CsvSupport.parseLong(columns.get(2), "sampleId", lineNumber),
                CsvSupport.parseEnum(columns.get(8), SealStatus.class, "status", lineNumber),
                columns.get(7),
                columns.get(9),
                CsvSupport.parseInstant(columns.get(5), "createdAt", lineNumber),
                CsvSupport.parseInstant(columns.get(6), "updatedAt", lineNumber),
                parseOwnerId(columns, lineNumber)
        );
    }

    private CustodyEvent parseCustodyEvent(List<String> columns, int lineNumber) {
        var createdAt = CsvSupport.parseInstant(columns.get(5), "createdAt", lineNumber);
        var updatedAt = columns.get(6).isBlank()
                ? createdAt
                : CsvSupport.parseInstant(columns.get(6), "updatedAt", lineNumber);

        return new CustodyEvent(
                CsvSupport.parseLong(columns.get(1), "id", lineNumber),
                CsvSupport.parseLong(columns.get(2), "sampleId", lineNumber),
                columns.get(10),
                columns.get(11),
                columns.get(12),
                CsvSupport.emptyToNull(columns.get(13)),
                CsvSupport.parseInstant(columns.get(14), "transferredAt", lineNumber),
                columns.get(9),
                createdAt,
                updatedAt,
                parseOwnerId(columns, lineNumber)
        );
    }

    private long parseOwnerId(List<String> columns, int lineNumber) {
        if (columns.size() <= 15 || columns.get(15).isBlank()) {
            return 0;
        }
        return CsvSupport.parseLong(columns.get(15), "ownerId", lineNumber);
    }

    private List<String> sampleColumns(Sample sample) {
        return List.of(
                "SAMPLE",
                Long.toString(sample.getId()),
                "",
                sample.getName(),
                sample.getHoldStatus().name(),
                sample.getCreatedAt().toString(),
                sample.getUpdatedAt().toString(),
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                Long.toString(sample.getOwnerId())
        );
    }

    private List<String> sealColumns(Seal seal) {
        return List.of(
                "SEAL",
                Long.toString(seal.getId()),
                Long.toString(seal.getSampleId()),
                "",
                "",
                seal.getCreatedAt().toString(),
                seal.getUpdatedAt().toString(),
                seal.getSealNumber(),
                seal.getStatus().name(),
                seal.getOwnerUsername(),
                "",
                "",
                "",
                "",
                "",
                Long.toString(seal.getOwnerId())
        );
    }

    private List<String> custodyColumns(CustodyEvent event) {
        return List.of(
                "CUSTODY",
                Long.toString(event.getId()),
                Long.toString(event.getSampleId()),
                "",
                "",
                event.getCreatedAt().toString(),
                event.getUpdatedAt().toString(),
                "",
                "",
                event.getOwnerUsername(),
                event.getFromUser(),
                event.getToUser(),
                event.getLocation(),
                CsvSupport.nullToEmpty(event.getComment()),
                event.getTransferredAt().toString(),
                Long.toString(event.getOwnerId())
        );
    }
}
