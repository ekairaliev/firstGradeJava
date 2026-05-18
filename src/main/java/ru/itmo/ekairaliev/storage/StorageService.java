package ru.itmo.ekairaliev.storage;

import ru.itmo.ekairaliev.service.CustodyService;
import ru.itmo.ekairaliev.service.SampleService;
import ru.itmo.ekairaliev.service.SealService;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class StorageService {
    private final SampleService sampleService;
    private final SealService sealService;
    private final CustodyService custodyService;
    private final CsvFileStorage csvFileStorage;
    private final FileValidator fileValidator;
    private final Path defaultPath;

    public StorageService(
            SampleService sampleService,
            SealService sealService,
            CustodyService custodyService,
            CsvFileStorage csvFileStorage,
            FileValidator fileValidator,
            Path defaultPath
    ) {
        this.sampleService = sampleService;
        this.sealService = sealService;
        this.custodyService = custodyService;
        this.csvFileStorage = csvFileStorage;
        this.fileValidator = fileValidator;
        this.defaultPath = defaultPath;
    }

    public void save(String rawPath) {
        Path path = resolvePath(rawPath);
        AppState state = new AppState(
                sampleService.getAll(),
                sealService.getAll(),
                custodyService.getAll()
        );
        csvFileStorage.save(path, state);
    }

    public void load(String rawPath) {
        Path path = resolvePath(rawPath);
        AppState loadedState = csvFileStorage.load(path);
        fileValidator.validate(loadedState);

        sampleService.replaceAll(loadedState.getSamples());
        sealService.replaceAll(loadedState.getSeals());
        custodyService.replaceAll(loadedState.getCustodyEvents());
    }

    public boolean hasDefaultPath() {
        return defaultPath != null;
    }

    public String getDefaultPath() {
        return defaultPath == null ? null : defaultPath.toString();
    }

    private Path resolvePath(String rawPath) {
        String normalized = rawPath == null ? null : rawPath.trim();
        if (normalized != null && !normalized.isEmpty()) {
            return Paths.get(normalized);
        }
        if (defaultPath != null) {
            return defaultPath;
        }
        throw new ValidationException("Ошибка: путь к файлу не указан");
    }
}
