package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandException;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;

import java.util.List;

public final class CustodyListCommand extends AbstractCommand {
    public CustodyListCommand() {
        super("cust_list", "cust_list <sample_id> [--last N]");
    }

    @Override
    public void validateArgs(List<String> args) {
        if (args.size() != 1 && args.size() != 3) {
            throw new CommandException("Ошибка: неверное число аргументов для " + getName());
        }

        parseId(args.get(0), "sample_id");
        if (args.size() == 3) {
            if (!"--last".equals(args.get(1))) {
                throw new CommandException("Ошибка: неизвестная опция '" + args.get(1) + "'");
            }
            parsePositiveInt(args.get(2));
        }
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        List<CustodyEvent> events;

        if (args.size() == 1) {
            events = context.getCustodyService().listBySample(sampleId);
        } else {
            int last = parsePositiveInt(args.get(2));
            events = context.getCustodyService().listBySampleLastN(sampleId, last);
        }

        if (events.isEmpty()) {
            System.out.println("Для sample " + sampleId + " пока нет custody events.");
            return CommandExecutionResult.CONTINUE;
        }

        System.out.printf("%-6s %-14s %-14s %-24s %-17s%n", "ID", "From", "To", "Location", "Time");
        for (CustodyEvent event : events) {
            System.out.printf(
                    "%-6d %-14s %-14s %-24s %-17s%n",
                    event.getId(),
                    context.trimForTable(event.getFromUser(), 14),
                    context.trimForTable(event.getToUser(), 14),
                    context.trimForTable(event.getLocation(), 24),
                    context.formatInstant(event.getTransferredAt())
            );
        }
        return CommandExecutionResult.CONTINUE;
    }
}
