package dev.nokee.core.exec.internal;

import dev.nokee.core.exec.CommandLineToolExecutionResult;
import dev.nokee.core.exec.CommandLineToolLogContent;
import dev.nokee.core.exec.ExecException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefaultCommandLineToolExecutionResult implements CommandLineToolExecutionResult {
	@Getter private final int exitValue;
	private final CommandLineToolLogContent output;
	private final CommandLineToolLogContent error;
	private final CommandLineToolLogContent fullOutput;
	private final Supplier<String> displayName;

	@Override
	public CommandLineToolLogContent getStandardOutput() {
		return output;
	}

	@Override
	public CommandLineToolLogContent getErrorOutput() {
		return error;
	}

	@Override
	public CommandLineToolLogContent getOutput() {
		return fullOutput;
	}

	@Override
	public CommandLineToolExecutionResult assertExitValueEquals(int expectedExitValue) throws ExecException {
		if (this.exitValue != expectedExitValue) {
			throw new ExecException(String.format("Process '%s' finished with unexpected exit value %d, was expecting %d", displayName.get(), exitValue, expectedExitValue));
		} else {
			return this;
		}
	}

	@Override
	public CommandLineToolExecutionResult assertNormalExitValue() throws ExecException {
		if (this.exitValue != 0) {
			throw new ExecException(String.format("Process '%s' finished with non-zero exit value %d\n%s", displayName.get(), exitValue, error.getAsString()));
		} else {
			return this;
		}
	}
}
