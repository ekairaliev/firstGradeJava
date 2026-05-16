package ru.itmo.ekairaliev.app;

import ru.itmo.ekairaliev.service.CustodyService;
import ru.itmo.ekairaliev.service.SampleService;
import ru.itmo.ekairaliev.service.SealService;
import ru.itmo.ekairaliev.storage.StorageService;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AppServices {
    private final SampleService sampleService;
    private final SealService sealService;
    private final CustodyService custodyService;
    private final StorageService storageService;
    private final Path startupPath;

    public AppServices(
            SampleService sampleService,
            SealService sealService,
            CustodyService custodyService,
            StorageService storageService,
            Path startupPath
    ) {
        this.sampleService = sampleService;
        this.sealService = sealService;
        this.custodyService = custodyService;
        this.storageService = storageService;
        this.startupPath = startupPath;
    }

    public SampleService getSampleService() {
        return sampleService;
    }

    public SealService getSealService() {
        return sealService;
    }

    public CustodyService getCustodyService() {
        return custodyService;
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public Path getStartupPath() {
        return startupPath;
    }

    public boolean autoLoadIfExists() {
        if (startupPath != null && Files.exists(startupPath)) {
            storageService.load(null);
            return true;
        }
        return false;
    }

    public static ValidationException invalidArgs() {
        return new ValidationException("Ошибка: при запуске можно передать только один путь к CSV-файлу");
    }
}
