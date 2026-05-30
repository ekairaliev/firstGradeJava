package ru.itmo.ekairaliev.app;

import ru.itmo.ekairaliev.service.AuthService;
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
    private final AuthService authService;
    private final Path startupPath;
    private final Path usersPath;
    private final boolean databaseMode;

    public AppServices(
            SampleService sampleService,
            SealService sealService,
            CustodyService custodyService,
            StorageService storageService,
            AuthService authService,
            Path startupPath,
            Path usersPath,
            boolean databaseMode
    ) {
        this.sampleService = sampleService;
        this.sealService = sealService;
        this.custodyService = custodyService;
        this.storageService = storageService;
        this.authService = authService;
        this.startupPath = startupPath;
        this.usersPath = usersPath;
        this.databaseMode = databaseMode;
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

    public AuthService getAuthService() {
        return authService;
    }

    public Path getStartupPath() {
        return startupPath;
    }

    public Path getUsersPath() {
        return usersPath;
    }

    public boolean isDatabaseMode() {
        return databaseMode;
    }

    public boolean autoLoadIfExists() {
        if (databaseMode) {
            return false;
        }
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
