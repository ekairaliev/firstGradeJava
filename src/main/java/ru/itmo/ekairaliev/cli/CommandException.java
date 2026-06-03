package ru.itmo.ekairaliev.cli;

public final class CommandException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CommandException(String message) {
        super(message);
    }
}
