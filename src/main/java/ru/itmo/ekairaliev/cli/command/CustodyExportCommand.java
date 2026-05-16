package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;

import java.util.List;

public final class CustodyExportCommand extends AbstractCommand {
    public CustodyExportCommand() {
        super("cust_export", "cust_export <sample_id>");
    }

    @Override
    public void validateArgs(List<String> args) {
        ensureArgCount(args, 1);
        parseId(args.get(0), "sample_id");
    }

    @Override
    public CommandExecutionResult execute(CliContext context, List<String> args) {
        long sampleId = parseId(args.get(0), "sample_id");
        Sample sample = context.getSampleService().getById(sampleId);
        List<CustodyEvent> events = context.getCustodyService().listBySampleChronological(sampleId);

        System.out.println("Sample " + sample.getId());
        System.out.println("name: " + sample.getName());
        System.out.println("holdStatus: " + sample.getHoldStatus());

        if (events.isEmpty()) {
            System.out.println("custody chain: no custody events");
        } else {
            System.out.println("custody chain:");
            for (CustodyEvent event : events) {
                System.out.println(
                        context.formatInstant(event.getTransferredAt())
                                + " | " + event.getFromUser()
                                + " -> " + event.getToUser()
                                + " | " + event.getLocation()
                                + " | " + (event.getComment() == null ? "-" : event.getComment())
                );
            }
        }

        System.out.println("Sample " + sampleId + " custody chain exported (text)");
        return CommandExecutionResult.CONTINUE;
    }
}
