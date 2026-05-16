package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Seal;

import java.util.List;

public final class SealAddCommand extends AbstractCommand {
    public SealAddCommand() {
        super("seal_add", "seal_add <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        context.getSampleService().getById(sampleId);

        String sealNumber = context.prompt("Номер пломбы");
        Seal seal = context.getSealService().add(sampleId, sealNumber, "SYSTEM");
        System.out.println("OK seal_id=" + seal.getId());
        return CommandExecutionResult.CONTINUE;
    }
}
