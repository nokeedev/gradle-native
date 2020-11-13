package dev.nokee.core.exec.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.*;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;

public abstract class AbstractCommandLineTool implements CommandLineTool {
	@Override
	public CommandLine withArguments(Object... arguments) {
		return new DefaultCommandLine(this, CommandLineToolArguments.of(arguments));
	}

	@Override
	public CommandLine withArguments(Iterable<?> arguments) {
		return new DefaultCommandLine(this, CommandLineToolArguments.of(ImmutableList.copyOf(arguments)));
	}

	@Override
	public CommandLineToolInvocationBuilder newInvocation() {
		return new DefaultCommandLine(this, CommandLineToolArguments.empty()).newInvocation();
	}

	@Override
	public <T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine) {
		return new DefaultCommandLine(this, CommandLineToolArguments.empty()).execute(engine);
	}

	@Override
	public ProcessBuilderEngine.Handle execute(@Nullable List<?> env, File workingDirectory) {
		return newInvocation()
			.workingDirectory(workingDirectory)
			.withEnvironmentVariables(from(env))
			.buildAndSubmit(new ProcessBuilderEngine());
	}
}
