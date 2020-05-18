package dev.nokee.core.exec;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.logging.Logger;

public class LoggingEngine<T extends CommandLineToolExecutionHandle> implements CommandLineToolExecutionEngine<T> {
	private static final Logger LOGGER = Logger.getLogger(LoggingEngine.class.getName());
	private final CommandLineToolExecutionEngine<T> delegate;

	private LoggingEngine(CommandLineToolExecutionEngine<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T submit(CommandLineToolInvocation invocation) {
		LOGGER.info(() -> String.format("Invoking process 'command '%s''. Command: %s", invocation.getTool().getExecutable(), String.join(" ", Iterables.concat(ImmutableList.of(invocation.getTool().getExecutable()), invocation.getArguments().get()))));
		return delegate.submit(invocation);
	}

	public static <T extends CommandLineToolExecutionHandle> LoggingEngine<T> wrap(CommandLineToolExecutionEngine<T> engine) {
		return new LoggingEngine<>(engine);
	}
}
