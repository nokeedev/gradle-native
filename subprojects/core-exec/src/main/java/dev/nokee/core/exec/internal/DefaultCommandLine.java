package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
	public CommandLineToolExecutionHandle execute(CommandLineToolExecutionEngine engine) {
		return newInvocation().buildAndSubmit(engine);
	}
}
