package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class SampleListCommand extends AbstractCommand {
    private static final Set<String> OPTIONS = Set.of("status", "owner", "name");

    public SampleListCommand() {
        super("sample_list", "sample_list [--status ACTIVE|ON_HOLD] [--owner ID] [--name TEXT]");
    }

    @Override
    public void validateArgs(List<String> args) {
        parseOptions(args);
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        List<Sample> samples = filterSamples(context.getSampleService().getAll(), parseOptions(args));
        if (samples.isEmpty()) {
            System.out.println("Список sample пуст.");
            return CommandExecutionResult.CONTINUE;
        }

        System.out.printf("%-6s %-30s %-12s %-8s %-17s%n", "ID", "Name", "Status", "OwnerID", "UpdatedAt");
        for (Sample sample : samples) {
            System.out.printf(
                    "%-6d %-30s %-12s %-8d %-17s%n",
                    sample.getId(),
                    context.trimForTable(sample.getName(), 30),
                    sample.getHoldStatus(),
                    sample.getOwnerId(),
                    context.formatInstant(sample.getUpdatedAt())
            );
        }
        return CommandExecutionResult.CONTINUE;
    }

    private CommandOptions parseOptions(List<String> args) {
        return CommandOptions.parse(args, OPTIONS);
    }

    private List<Sample> filterSamples(List<Sample> samples, CommandOptions options) {
        Stream<Sample> stream = samples.stream();

        if (options.value("status").isPresent()) {
            SampleHoldStatus status = options.enumValue("status", SampleHoldStatus.class);
            stream = stream.filter(sample -> sample.getHoldStatus() == status);
        }
        if (options.value("owner").isPresent()) {
            long ownerId = options.positiveLong("owner", "owner");
            stream = stream.filter(sample -> sample.getOwnerId() == ownerId);
        }
        if (options.value("name").isPresent()) {
            String name = options.value("name").orElseThrow();
            stream = stream.filter(sample -> CommandOptions.containsIgnoreCase(sample.getName(), name));
        }

        return stream.toList();
    }
}
