package ru.itmo.ekairaliev.cli.command;

import ru.itmo.ekairaliev.cli.CliContext;
import ru.itmo.ekairaliev.cli.CommandExecutionResult;
import ru.itmo.ekairaliev.model.CustodyEvent;
import ru.itmo.ekairaliev.model.Sample;
import ru.itmo.ekairaliev.model.SampleHoldStatus;
import ru.itmo.ekairaliev.validation.ValidationException;

import java.util.List;

public final class CustodyAddCommand extends AbstractCommand {
    public CustodyAddCommand() {
        super("cust_add", "cust_add <sample_id>");
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
        if (sample.getHoldStatus() == SampleHoldStatus.ON_HOLD) {
            throw new ValidationException("Ошибка: sample с id=" + sampleId + " находится ON_HOLD, сначала выполните sample_release");
        }

        String fromUser = context.prompt("От кого (только имя)");
        String toUser = context.prompt("Кому (только имя)");
        String location = context.prompt("Место");
        String comment = context.prompt("Комментарий (можно пусто)");

        CustodyEvent event = context.getCustodyService().add(
                sampleId,
                fromUser,
                toUser,
                location,
                context.blankToNull(comment),
                "SYSTEM"
        );
        System.out.println("OK event_id=" + event.getId());
        return CommandExecutionResult.CONTINUE;
    }
}
