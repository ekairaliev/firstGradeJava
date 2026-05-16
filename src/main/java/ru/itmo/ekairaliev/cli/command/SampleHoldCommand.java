package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;

import java.util.List;

public final class SampleHoldCommand extends AbstractCommand {
    public SampleHoldCommand() {
        super("sample_hold", "sample_hold <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        context.getSampleService().hold(sampleId);
        System.out.println("OK sample " + sampleId + " is ON_HOLD");
        return CommandExecutionResult.CONTINUE;
    }
}
