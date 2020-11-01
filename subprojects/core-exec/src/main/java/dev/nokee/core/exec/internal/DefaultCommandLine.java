package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;

import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.from;

@EqualsAndHashCode
@RequiredArgsConstructor
public class DefaultCommandLine implements CommandLine {
	@Getter private final CommandLineTool tool;
	@Getter private final CommandLineToolArguments arguments;

	@Override
	public CommandLineToolInvocationBuilder newInvocation() {
		return new DefaultCommandLineToolInvocationBuilder(this);
	}

	@Override
	public <T extends CommandLineToolExecutionHandle> T execute(CommandLineToolExecutionEngine<T> engine) {
		return newInvocation().buildAndSubmit(engine);
	}

	public ProcessBuilderEngine.Handle execute(List<?> env, File workingDirectory) {
		return newInvocation()
			.workingDirectory(workingDirectory)
			.withEnvironmentVariables(from(env))
			.buildAndSubmit(new ProcessBuilderEngine());
	}

	@Override
	public ProcessBuilderEngine.Handle execute() {
		return execute(new ProcessBuilderEngine());
	}
}
