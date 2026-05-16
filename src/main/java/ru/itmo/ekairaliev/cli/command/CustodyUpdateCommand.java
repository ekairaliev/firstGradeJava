package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;

import java.util.List;

public final class CustodyUpdateCommand extends AbstractCommand {
    public CustodyUpdateCommand() {
        super("cust_update", "cust_update <event_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "event_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long eventId = parseId(args.get(0), "event_id");
        String fromUser = context.prompt("От кого (только имя)");
        String toUser = context.prompt("Кому (только имя)");
        String location = context.prompt("Место");
        String comment = context.prompt("Комментарий (можно пусто)");

        CustodyEvent event = context.getCustodyService().update(
                eventId,
                fromUser,
                toUser,
                location,
                context.blankToNull(comment)
        );
        System.out.println("OK event_id=" + event.getId() + " updated");
        return CommandExecutionResult.CONTINUE;
    }
}
