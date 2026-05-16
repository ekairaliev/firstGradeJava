package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Sample;

import java.util.List;

public final class SampleShowCommand extends AbstractCommand {
    public SampleShowCommand() {
        super("sample_show", "sample_show <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        Sample sample = context.getSampleService().getById(sampleId);

        System.out.println("Sample #" + sample.getId());
        System.out.println("name: " + sample.getName());
        System.out.println("holdStatus: " + sample.getHoldStatus());
        System.out.println("createdAt: " + context.formatInstant(sample.getCreatedAt()));
        System.out.println("updatedAt: " + context.formatInstant(sample.getUpdatedAt()));
        return CommandExecutionResult.CONTINUE;
    }
}
