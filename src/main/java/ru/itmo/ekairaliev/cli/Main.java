package ru.itmo.ekairaliev.cli;

import ru.itmo.ekairaliev.app.AppBootstrap;
import ru.itmo.ekairaliev.app.AppServices;
import ru.itmo.ekairaliev.cli.command.CustodyAddCommand;
import ru.itmo.ekairaliev.cli.command.CustodyCheckCommand;
import ru.itmo.ekairaliev.cli.command.CustodyExportCommand;
import ru.itmo.ekairaliev.cli.command.CustodyListCommand;
import ru.itmo.ekairaliev.cli.command.CustodyRemoveCommand;
import ru.itmo.ekairaliev.cli.command.CustodyShowCommand;
import ru.itmo.ekairaliev.cli.command.CustodyUpdateCommand;
import ru.itmo.ekairaliev.cli.command.ExitCommand;
import ru.itmo.ekairaliev.cli.command.HelpCommand;
import ru.itmo.ekairaliev.cli.command.HistoryCommand;
import ru.itmo.ekairaliev.cli.command.LoadCommand;
import ru.itmo.ekairaliev.cli.command.LoginCommand;
import ru.itmo.ekairaliev.cli.command.RegisterCommand;
import ru.itmo.ekairaliev.cli.command.SampleAddCommand;
import ru.itmo.ekairaliev.cli.command.SampleHoldCommand;
import ru.itmo.ekairaliev.cli.command.SampleListCommand;
import ru.itmo.ekairaliev.cli.command.SampleRemoveCommand;
import ru.itmo.ekairaliev.cli.command.SampleReleaseCommand;
import ru.itmo.ekairaliev.cli.command.SampleShowCommand;
import ru.itmo.ekairaliev.cli.command.SampleUpdateCommand;
import ru.itmo.ekairaliev.cli.command.SaveCommand;
import ru.itmo.ekairaliev.cli.command.SealAddCommand;
import ru.itmo.ekairaliev.cli.command.SealBreakCommand;
import ru.itmo.ekairaliev.cli.command.SealListCommand;
import ru.itmo.ekairaliev.cli.command.SealRemoveCommand;
import ru.itmo.ekairaliev.cli.command.SealShowCommand;
import ru.itmo.ekairaliev.cli.command.SealUpdateCommand;
import ru.itmo.ekairaliev.cli.command.StatsCommand;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.util.List;
import java.util.Scanner;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        final AppServices services;
        try {
            services = AppBootstrap.create(args);
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
            return;
        }

        CommandRegistry commandRegistry = new CommandRegistry(List.of(
                new HelpCommand(),
                new HistoryCommand(),
                new StatsCommand(),
                StatsCommand.samples(),
                StatsCommand.seals(),
                StatsCommand.custody(),
                new ExitCommand(),
                new RegisterCommand(),
                new LoginCommand(),
                new SaveCommand(),
                new LoadCommand(),
                new SampleAddCommand(),
                new SampleListCommand(),
                new SampleShowCommand(),
                new SampleUpdateCommand(),
                new SampleRemoveCommand(),
                new SampleHoldCommand(),
                new SampleReleaseCommand(),
                new SealAddCommand(),
                new SealListCommand(),
                new SealShowCommand(),
                new SealUpdateCommand(),
                new SealRemoveCommand(),
                new SealBreakCommand(),
                new CustodyAddCommand(),
                new CustodyListCommand(),
                new CustodyShowCommand(),
                new CustodyUpdateCommand(),
                new CustodyRemoveCommand(),
                new CustodyCheckCommand(),
                new CustodyExportCommand()
        ));

        CliContext cliContext = new CliContext(
                services.getSampleService(),
                services.getSealService(),
                services.getCustodyService(),
                services.getAuthService(),
                commandRegistry,
                services.getStorageService(),
                new Scanner(System.in)
        );

        try {
            if (services.autoLoadIfExists()) {
                System.out.println("Автозагрузка выполнена: " + services.getStartupPath());
            }
        } catch (ValidationException e) {
            System.out.println(e.getMessage());
        }

        new CliApplication(cliContext).run();
    }
}
