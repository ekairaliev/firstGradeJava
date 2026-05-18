package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Sample;

import java.util.List;

public final class SampleUpdateCommand extends AbstractCommand {
    public SampleUpdateCommand() {
        super("sample_update", "sample_update <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        String newName = context.prompt("Новое название sample");

        Sample sample = context.getSampleService().update(sampleId, newName);
        System.out.println("OK sample_id=" + sample.getId() + " updated");
        return CommandExecutionResult.CONTINUE;
    }
}
