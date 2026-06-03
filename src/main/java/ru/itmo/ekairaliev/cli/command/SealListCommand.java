package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class SealListCommand extends AbstractCommand {
    private static final Set<String> OPTIONS = Set.of("sample", "status", "owner", "owner-name", "number");

    public SealListCommand() {
        super("seal_list", "seal_list [--sample ID] [--status ACTIVE|BROKEN] [--owner ID] [--owner-name TEXT] [--number TEXT]");
    }

    @Override
    public void validateArgs(List<String> args) {
        parseOptions(args);
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        List<Seal> seals = filterSeals(context.getSealService().getAll(), parseOptions(args));
        if (seals.isEmpty()) {
            System.out.println("Список seal пуст.");
            return CommandExecutionResult.CONTINUE;
        }

        System.out.printf("%-6s %-10s %-20s %-10s %-14s %-8s %-17s%n",
                "ID", "SampleID", "SealNumber", "Status", "Owner", "OwnerID", "UpdatedAt");
        for (Seal seal : seals) {
            System.out.printf(
                    "%-6d %-10d %-20s %-10s %-14s %-8d %-17s%n",
                    seal.getId(),
                    seal.getSampleId(),
                    context.trimForTable(seal.getSealNumber(), 20),
                    seal.getStatus(),
                    context.trimForTable(seal.getOwnerUsername(), 14),
                    seal.getOwnerId(),
                    context.formatInstant(seal.getUpdatedAt())
            );
        }
        return CommandExecutionResult.CONTINUE;
    }

    private CommandOptions parseOptions(List<String> args) {
        return CommandOptions.parse(args, OPTIONS);
    }

    private List<Seal> filterSeals(List<Seal> seals, CommandOptions options) {
        Stream<Seal> stream = seals.stream();

        if (options.value("sample").isPresent()) {
            long sampleId = options.positiveLong("sample", "sample");
            stream = stream.filter(seal -> seal.getSampleId() == sampleId);
        }
        if (options.value("status").isPresent()) {
            SealStatus status = options.enumValue("status", SealStatus.class);
            stream = stream.filter(seal -> seal.getStatus() == status);
        }
        if (options.value("owner").isPresent()) {
            long ownerId = options.positiveLong("owner", "owner");
            stream = stream.filter(seal -> seal.getOwnerId() == ownerId);
        }
        if (options.value("owner-name").isPresent()) {
            String ownerName = options.value("owner-name").orElseThrow();
            stream = stream.filter(seal -> CommandOptions.containsIgnoreCase(seal.getOwnerUsername(), ownerName));
        }
        if (options.value("number").isPresent()) {
            String number = options.value("number").orElseThrow();
            stream = stream.filter(seal -> CommandOptions.containsIgnoreCase(seal.getSealNumber(), number));
        }

        return stream.toList();
    }
}
