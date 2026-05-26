package ru.itmo.ekairaliev.app;

import ru.itmo.ekairaliev.db.DatabaseConfig;
import ru.itmo.ekairaliev.db.JdbcConnectionFactory;
import ru.itmo.ekairaliev.db.PostgresSchemaInitializer;
import ru.itmo.ekairaliev.repository.FileUserRepository;
import ru.itmo.ekairaliev.repository.PostgresCustodyEventRepository;
import ru.itmo.ekairaliev.repository.PostgresSampleRepository;
import ru.itmo.ekairaliev.repository.PostgresSealRepository;
import ru.itmo.ekairaliev.repository.PostgresUserRepository;
import ru.itmo.ekairaliev.repository.UserRepository;
import ru.itmo.ekairaliev.service.AuthService;
import ru.itmo.ekairaliev.service.CustodyService;
import ru.itmo.ekairaliev.service.SampleService;
import ru.itmo.ekairaliev.service.SealService;
import ru.itmo.ekairaliev.storage.CsvFileStorage;
import ru.itmo.ekairaliev.storage.FileValidator;
import ru.itmo.ekairaliev.storage.StorageService;
import ru.itmo.ekairaliev.storage.UserFileStorage;
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
        Path usersPath = usersPathFor(startupPath);

        var databaseConfig = DatabaseConfig.fromEnvironment();
        UserRepository userRepository;
        if (databaseConfig.isPresent()) {
            JdbcConnectionFactory connectionFactory = new JdbcConnectionFactory(databaseConfig.get());
            new PostgresSchemaInitializer(connectionFactory).initialize();

            sampleService.bindRepository(new PostgresSampleRepository(connectionFactory));
            sealService.bindRepository(new PostgresSealRepository(connectionFactory));
            custodyService.bindRepository(new PostgresCustodyEventRepository(connectionFactory));
            userRepository = new PostgresUserRepository(connectionFactory);
        } else {
            userRepository = new FileUserRepository(new UserFileStorage(), usersPath);
        }

        AuthService authService = new AuthService(userRepository);
        StorageService storageService = new StorageService(
                sampleService,
                sealService,
                custodyService,
                new CsvFileStorage(),
                new FileValidator(),
                startupPath
        );

        return new AppServices(
                sampleService,
                sealService,
                custodyService,
                storageService,
                authService,
                startupPath,
                usersPath,
                databaseConfig.isPresent()
        );
    }

    public static AppServices create(String[] args) {
        return create(List.of(args));
    }

    private static Path usersPathFor(Path startupPath) {
        if (startupPath == null) {
            return Paths.get("users.csv");
        }

        Path absolute = startupPath.toAbsolutePath();
        Path parent = absolute.getParent();
        String fileName = absolute.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        Path usersFile = Paths.get(baseName + "_users.csv");
        return parent == null ? usersFile : parent.resolve(usersFile);
    }
}
