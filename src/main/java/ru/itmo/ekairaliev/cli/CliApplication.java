package ru.itmo.ekairaliev.cli;

import ru.itmo.ekairaliev.cli.command.AbstractCommand;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.util.List;

public final class CliApplication {
    private final CliContext context;

    public CliApplication(CliContext context) {
        this.context = context;
    }

    public void run() {
        System.out.println("Chain of Custody");
        System.out.println("Введите help, чтобы увидеть список команд.");

        while (true) {
            System.out.print("> ");

            if (!context.getScanner().hasNextLine()) {
                System.out.println("Ввод завершен.");
                return;
            }

            String line = context.getScanner().nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                CommandExecutionResult result = execute(line);
                if (result == CommandExecutionResult.EXIT) {
                    System.out.println("Работа завершена.");
                    return;
                }
            } catch (ValidationException | CommandException e) {
                System.out.println(e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
    }

    private CommandExecutionResult execute(String line) {
        List<String> tokens = CommandTokenizer.tokenize(line);
        if (tokens.isEmpty()) {
            return CommandExecutionResult.CONTINUE;
        }

        context.rememberCommand(line);
        String commandName = tokens.get(0);
        List<String> args = List.copyOf(tokens.subList(1, tokens.size()));

        AbstractCommand command = context.getCommandRegistry()
                .find(commandName)
                .orElseThrow(() -> new CommandException("Ошибка: неизвестная команда '" + commandName + "'"));

        command.validateArgs(args);
        return command.execute(context, args);
    }
}
