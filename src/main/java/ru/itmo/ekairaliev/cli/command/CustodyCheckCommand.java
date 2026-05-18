package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;

import java.util.List;
import java.util.Optional;

public final class CustodyCheckCommand extends AbstractCommand {
    public CustodyCheckCommand() {
        super("cust_check", "cust_check <sample_id>");
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

        Optional<String> currentOwner = context.getCustodyService().currentOwner(sampleId);
        if (currentOwner.isEmpty()) {
            System.out.println("Warning: no custody events");
            return CommandExecutionResult.CONTINUE;
        }

        System.out.println("OK current owner: " + currentOwner.get());
        return CommandExecutionResult.CONTINUE;
    }
}
