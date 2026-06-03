package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandException;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class CustodyListCommand extends AbstractCommand {
    private static final Set<String> OPTIONS = Set.of("last", "from", "to", "location", "owner", "owner-name");

    public CustodyListCommand() {
        super("cust_list", "cust_list [sample_id] [--last N] [--from TEXT] [--to TEXT] [--location TEXT] [--owner ID] [--owner-name TEXT]");
    }

    @Override
    public void validateArgs(List<String> args) {
        parseArgs(args);
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        ParsedArgs parsedArgs = parseArgs(args);
        List<CustodyEvent> events = parsedArgs.sampleId == null
                ? context.getCustodyService().getAll()
                : context.getCustodyService().listBySample(parsedArgs.sampleId);
        events = filterEvents(events, parsedArgs.options);

        if (events.isEmpty()) {
            if (parsedArgs.sampleId == null) {
                System.out.println("Список custody events пуст.");
            } else {
                System.out.println("Для sample " + parsedArgs.sampleId + " пока нет custody events.");
            }
            return CommandExecutionResult.CONTINUE;
        }

        System.out.printf("%-6s %-10s %-14s %-14s %-24s %-14s %-8s %-17s%n",
                "ID", "SampleID", "From", "To", "Location", "Owner", "OwnerID", "Time");
        for (CustodyEvent event : events) {
            System.out.printf(
                    "%-6d %-10d %-14s %-14s %-24s %-14s %-8d %-17s%n",
                    event.getId(),
                    event.getSampleId(),
                    context.trimForTable(event.getFromUser(), 14),
                    context.trimForTable(event.getToUser(), 14),
                    context.trimForTable(event.getLocation(), 24),
                    context.trimForTable(event.getOwnerUsername(), 14),
                    event.getOwnerId(),
                    context.formatInstant(event.getTransferredAt())
            );
        }
        return CommandExecutionResult.CONTINUE;
    }

    private ParsedArgs parseArgs(List<String> args) {
        Long sampleId = null;
        int optionStartIndex = 0;
        if (!args.isEmpty() && !args.get(0).startsWith("--")) {
            sampleId = parseId(args.get(0), "sample_id");
            if (sampleId <= 0) {
                throw new CommandException("Ошибка: sample_id должен быть > 0");
            }
            optionStartIndex = 1;
        }

        CommandOptions options = CommandOptions.parse(args.subList(optionStartIndex, args.size()), OPTIONS);
        if (options.value("last").isPresent()) {
            options.positiveInt("last", "last");
        }
        if (options.value("owner").isPresent()) {
            options.positiveLong("owner", "owner");
        }
        return new ParsedArgs(sampleId, options);
    }

    private List<CustodyEvent> filterEvents(List<CustodyEvent> events, CommandOptions options) {
        Stream<CustodyEvent> stream = events.stream();

        if (options.value("from").isPresent()) {
            String from = options.value("from").orElseThrow();
            stream = stream.filter(event -> CommandOptions.containsIgnoreCase(event.getFromUser(), from));
        }
        if (options.value("to").isPresent()) {
            String to = options.value("to").orElseThrow();
            stream = stream.filter(event -> CommandOptions.containsIgnoreCase(event.getToUser(), to));
        }
        if (options.value("location").isPresent()) {
            String location = options.value("location").orElseThrow();
            stream = stream.filter(event -> CommandOptions.containsIgnoreCase(event.getLocation(), location));
        }
        if (options.value("owner").isPresent()) {
            long ownerId = options.positiveLong("owner", "owner");
            stream = stream.filter(event -> event.getOwnerId() == ownerId);
        }
        if (options.value("owner-name").isPresent()) {
            String ownerName = options.value("owner-name").orElseThrow();
            stream = stream.filter(event -> CommandOptions.containsIgnoreCase(event.getOwnerUsername(), ownerName));
        }

        List<CustodyEvent> filteredEvents = stream.toList();
        if (options.value("last").isPresent()) {
            int last = options.positiveInt("last", "last");
            return filteredEvents.stream()
                    .limit(last)
                    .toList();
        }
        return filteredEvents;
    }

    private record ParsedArgs(Long sampleId, CommandOptions options) {
    }
}
