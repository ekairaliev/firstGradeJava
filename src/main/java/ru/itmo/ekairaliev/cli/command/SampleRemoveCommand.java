package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Sample;

import java.util.List;

public final class SampleRemoveCommand extends AbstractCommand {
    public SampleRemoveCommand() {
        super("sample_remove", "sample_remove <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        Sample sample = context.getSampleService().remove(sampleId);
        System.out.println("OK sample_id=" + sample.getId() + " removed");
        return CommandExecutionResult.CONTINUE;
    }
}
