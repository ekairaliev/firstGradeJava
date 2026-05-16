package ru.itmo.ekairaliev.app;

import ru.itmo.ekairaliev.service.CustodyService;
import ru.itmo.ekairaliev.service.SampleService;
import ru.itmo.ekairaliev.service.SealService;
import ru.itmo.ekairaliev.storage.CsvFileStorage;
import ru.itmo.ekairaliev.storage.FileValidator;
import ru.itmo.ekairaliev.storage.StorageService;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class AppBootstrap {
    private AppBootstrap() {
    }

    public static AppServices create(List<String> args) {
        if (args.size() > 1) {
            throw AppServices.invalidArgs();
        }

        SampleService sampleService = new SampleService();
        SealService sealService = new SealService(sampleService);
        CustodyService custodyService = new CustodyService(sampleService);
        sampleService.bindRelations(sealService, custodyService);

        Path startupPath = args.isEmpty() ? null : Paths.get(args.get(0));
        StorageService storageService = new StorageService(
                sampleService,
                sealService,
                custodyService,
                new CsvFileStorage(),
                new FileValidator(),
                startupPath
        );

        return new AppServices(sampleService, sealService, custodyService, storageService, startupPath);
    }

    public static AppServices create(String[] args) {
        return create(List.of(args));
    }
}
