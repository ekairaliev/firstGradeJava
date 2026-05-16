package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;

import java.util.List;

public final class CustodyRemoveCommand extends AbstractCommand {
    public CustodyRemoveCommand() {
        super("cust_remove", "cust_remove <event_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "event_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long eventId = parseId(args.get(0), "event_id");
        CustodyEvent event = context.getCustodyService().remove(eventId);
        System.out.println("OK event_id=" + event.getId() + " removed");
        return CommandExecutionResult.CONTINUE;
    }
}
