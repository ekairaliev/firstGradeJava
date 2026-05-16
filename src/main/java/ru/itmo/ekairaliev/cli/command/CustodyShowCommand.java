package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;

import java.util.List;

public final class CustodyShowCommand extends AbstractCommand {
    public CustodyShowCommand() {
        super("cust_show", "cust_show <event_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "event_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long eventId = parseId(args.get(0), "event_id");
        CustodyEvent event = context.getCustodyService().getById(eventId);

        System.out.println("CustodyEvent #" + event.getId());
        System.out.println("sample_id: " + event.getSampleId());
        System.out.println("from: " + event.getFromUser());
        System.out.println("to: " + event.getToUser());
        System.out.println("location: " + event.getLocation());
        System.out.println("comment: " + (event.getComment() == null ? "-" : event.getComment()));
        return CommandExecutionResult.CONTINUE;
    }
}
