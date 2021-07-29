package dev.nokee.model.streams;

import java.util.function.Supplier;

interface TerminalSink<T, R> extends Sink<T>, Supplier<R> {
}
