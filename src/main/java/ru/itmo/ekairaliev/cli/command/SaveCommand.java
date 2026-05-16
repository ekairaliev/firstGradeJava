package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandException;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;

import java.util.List;

public final class SaveCommand extends AbstractCommand {
    public SaveCommand() {
        super("save", "save [path]");
    }

    @Override
    public void validateArgs(List<String> args) {
        if (args.size() > 1) {
            throw new CommandException("Ошибка: неверное число аргументов для " + getName());
        }
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        String path = args.isEmpty() ? null : args.get(0);
        context.getStorageService().save(path);
        System.out.println("OK saved");
        return CommandExecutionResult.CONTINUE;
    }
}
