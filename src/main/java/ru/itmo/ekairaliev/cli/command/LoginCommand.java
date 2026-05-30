package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.User;

import java.util.List;

public final class LoginCommand extends AbstractCommand {
    public LoginCommand() {
        super("login", "login");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 0);
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        String login = context.prompt("Логин");
        String password = context.prompt("Пароль");
        User user = context.getAuthService().login(login, password);
        System.out.println("OK logged in as " + user.getLogin());
        return CommandExecutionResult.CONTINUE;
    }
}
