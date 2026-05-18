package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;

import java.util.List;

public final class SealBreakCommand extends AbstractCommand {
    public SealBreakCommand() {
        super("seal_break", "seal_break <seal_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "seal_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sealId = parseId(args.get(0), "seal_id");
        context.getSealService().breakSeal(sealId);
        System.out.println("OK seal " + sealId + " is BROKEN");
        return CommandExecutionResult.CONTINUE;
    }
}
