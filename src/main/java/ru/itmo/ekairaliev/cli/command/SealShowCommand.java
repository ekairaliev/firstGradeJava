package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Seal;

import java.util.List;

public final class SealShowCommand extends AbstractCommand {
    public SealShowCommand() {
        super("seal_show", "seal_show <seal_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "seal_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sealId = parseId(args.get(0), "seal_id");
        Seal seal = context.getSealService().getById(sealId);

        System.out.println("Seal #" + seal.getId());
        System.out.println("sample_id: " + seal.getSampleId());
        System.out.println("sealNumber: " + seal.getSealNumber());
        System.out.println("status: " + seal.getStatus());
        return CommandExecutionResult.CONTINUE;
    }
}
