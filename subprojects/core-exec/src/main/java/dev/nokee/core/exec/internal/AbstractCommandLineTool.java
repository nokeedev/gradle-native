package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.*;

import java.util.Arrays;

public abstract class AbstractCommandLineTool implements CommandLineTool {
	@Override
	public CommandLine withArguments(Object... arguments) {
		return new DefaultCommandLine(this, new DefaultCommandLineToolArguments(Arrays.asList(arguments)));
	}

	@Override
	public CommandLine withArguments(Iterable<?> arguments) {
		return new DefaultCommandLine(this, new DefaultCommandLineToolArguments(ImmutableList.copyOf(arguments)));
	}

	@Override
	public CommandLineToolInvocationBuilder newInvocation() {
		return new DefaultCommandLine(this, EmptyCommandLineToolArguments.INSTANCE).newInvocation();
	}

	@Override
	public <T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine) {
		return new DefaultCommandLine(this, EmptyCommandLineToolArguments.INSTANCE).execute(engine);
	}
}
