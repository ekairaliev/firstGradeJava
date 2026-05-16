package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Seal;

import java.util.List;

public final class SealRemoveCommand extends AbstractCommand {
    public SealRemoveCommand() {
        super("seal_remove", "seal_remove <seal_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "seal_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sealId = parseId(args.get(0), "seal_id");
        Seal seal = context.getSealService().remove(sealId);
        System.out.println("OK seal_id=" + seal.getId() + " removed");
        return CommandExecutionResult.CONTINUE;
    }
}
