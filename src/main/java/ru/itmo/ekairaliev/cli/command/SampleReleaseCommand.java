package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;

import java.util.List;

public final class SampleReleaseCommand extends AbstractCommand {
    public SampleReleaseCommand() {
        super("sample_release", "sample_release <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        context.getSampleService().release(sampleId);
        System.out.println("OK sample " + sampleId + " is ACTIVE");
        return CommandExecutionResult.CONTINUE;
    }
}
