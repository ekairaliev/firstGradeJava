package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandException;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.model.Seal;
import ru.itmo.ekairaliev.model.SealStatus;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class StatsCommand extends AbstractCommand {
    private static final Set<String> CATEGORIES = Set.of("all", "samples", "seals", "custody");
    private final String fixedCategory;

    public StatsCommand() {
        this("stats", "stats [all|samples|seals|custody]", null);
    }

    public static StatsCommand samples() {
        return new StatsCommand("sample_stats", "sample_stats", "samples");
    }

    public static StatsCommand seals() {
        return new StatsCommand("seal_stats", "seal_stats", "seals");
    }

    public static StatsCommand custody() {
        return new StatsCommand("cust_stats", "cust_stats", "custody");
    }

    private StatsCommand(String name, String help, String fixedCategory) {
        super(name, help);
        this.fixedCategory = fixedCategory;
    }

    @Override
    public void validateArgs(List<String> args) {
        if (fixedCategory != null) {
            ensureArgCount(args, 0);
            return;
        }
        if (args.size() > 1) {
            throw new CommandException("Ошибка: неверное число аргументов для " + getName());
        }
        if (!args.isEmpty() && !CATEGORIES.contains(args.get(0).toLowerCase(Locale.ROOT))) {
            throw new CommandException("Ошибка: stats поддерживает категории: all, samples, seals, custody");
        }
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        String category = fixedCategory == null
                ? args.isEmpty() ? "all" : args.get(0).toLowerCase(Locale.ROOT)
                : fixedCategory;
        if ("all".equals(category) || "samples".equals(category)) {
            printSampleStats(context.getSampleService().getAll());
        }
        if ("all".equals(category) || "seals".equals(category)) {
            printSealStats(context.getSealService().getAll());
        }
        if ("all".equals(category) || "custody".equals(category)) {
            printCustodyStats(context.getSampleService().getAll(), context.getCustodyService().getAll());
        }
        return CommandExecutionResult.CONTINUE;
    }

    private void printSampleStats(List<Sample> samples) {
        long active = samples.stream()
                .filter(sample -> sample.getHoldStatus() == SampleHoldStatus.ACTIVE)
                .count();
        long onHold = samples.stream()
                .filter(sample -> sample.getHoldStatus() == SampleHoldStatus.ON_HOLD)
                .count();
        long withOwner = samples.stream()
                .filter(sample -> sample.getOwnerId() > 0)
                .count();

        System.out.println("Samples");
        System.out.println("total: " + samples.size());
        System.out.println("ACTIVE: " + active);
        System.out.println("ON_HOLD: " + onHold);
        System.out.println("withOwner: " + withOwner);
    }

    private void printSealStats(List<Seal> seals) {
        long active = seals.stream()
                .filter(seal -> seal.getStatus() == SealStatus.ACTIVE)
                .count();
        long broken = seals.stream()
                .filter(seal -> seal.getStatus() == SealStatus.BROKEN)
                .count();
        long linkedSamples = seals.stream()
                .map(Seal::getSampleId)
                .distinct()
                .count();

        System.out.println("Seals");
        System.out.println("total: " + seals.size());
        System.out.println("ACTIVE: " + active);
        System.out.println("BROKEN: " + broken);
        System.out.println("samplesWithSeals: " + linkedSamples);
    }

    private void printCustodyStats(List<Sample> samples, List<CustodyEvent> events) {
        long samplesWithEvents = events.stream()
                .map(CustodyEvent::getSampleId)
                .distinct()
                .count();
        long withComments = events.stream()
                .filter(event -> event.getComment() != null && !event.getComment().isBlank())
                .count();
        double averageEventsPerSample = samples.isEmpty() ? 0.0 : (double) events.size() / samples.size();
        long maxEventsForSample = eventsBySample(events).values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        System.out.println("Custody");
        System.out.println("total: " + events.size());
        System.out.println("samplesWithEvents: " + samplesWithEvents);
        System.out.println("eventsWithComments: " + withComments);
        System.out.printf(Locale.ROOT, "avgEventsPerSample: %.2f%n", averageEventsPerSample);
        System.out.println("maxEventsForOneSample: " + maxEventsForSample);
    }

    private Map<Long, Long> eventsBySample(List<CustodyEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(CustodyEvent::getSampleId, Collectors.counting()));
    }
}
