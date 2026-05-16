package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Seal;

import java.util.List;

public final class SealUpdateCommand extends AbstractCommand {
    public SealUpdateCommand() {
        super("seal_update", "seal_update <seal_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "seal_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sealId = parseId(args.get(0), "seal_id");
        String sealNumber = context.prompt("Новый номер пломбы");

        Seal seal = context.getSealService().update(sealId, sealNumber);
        System.out.println("OK seal_id=" + seal.getId() + " updated");
        return CommandExecutionResult.CONTINUE;
    }
}
