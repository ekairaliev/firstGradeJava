package ru.itmo.ekairaliev.cli;

import ru.itmo.ekairaliev.service.AuthService;
import ru.itmo.ekairaliev.service.CustodyService;
import ru.itmo.ekairaliev.service.SampleService;
import ru.itmo.ekairaliev.service.SealService;
import ru.itmo.ekairaliev.storage.StorageService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

public final class CliContext {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withLocale(Locale.ROOT)
                    .withZone(ZoneId.systemDefault());

    private final SampleService sampleService;
    private final SealService sealService;
    private final CustodyService custodyService;
    private final AuthService authService;
    private final CommandRegistry commandRegistry;
    private final StorageService storageService;
    private final Scanner scanner;
    private final List<String> commandHistory = new ArrayList<>();

    public CliContext(
            SampleService sampleService,
            SealService sealService,
            CustodyService custodyService,
            AuthService authService,
            CommandRegistry commandRegistry,
            StorageService storageService,
            Scanner scanner
    ) {
        this.sampleService = Objects.requireNonNull(sampleService);
        this.sealService = Objects.requireNonNull(sealService);
        this.custodyService = Objects.requireNonNull(custodyService);
        this.authService = Objects.requireNonNull(authService);
        this.commandRegistry = Objects.requireNonNull(commandRegistry);
        this.storageService = Objects.requireNonNull(storageService);
        this.scanner = Objects.requireNonNull(scanner);
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

    public AuthService getAuthService() {
        return authService;
    }

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public StorageService getStorageService() {
        return storageService;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public void rememberCommand(String commandLine) {
        commandHistory.add(Objects.requireNonNull(commandLine));
    }

    public List<String> recentCommands() {
        return List.copyOf(commandHistory);
    }

    public String prompt(String label) {
        System.out.print(label + ": ");
        if (!scanner.hasNextLine()) {
            throw new CommandException("Ошибка: ввод неожиданно завершился");
        }
        return scanner.nextLine();
    }

    public String formatInstant(Instant instant) {
        return DATE_TIME_FORMATTER.format(instant);
    }

    public String trimForTable(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    public String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
