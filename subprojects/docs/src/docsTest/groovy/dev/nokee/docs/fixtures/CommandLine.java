package dev.nokee.docs.fixtures;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandLine {
	private final String executable;
	private final List<String> arguments;

	private CommandLine(String executable, List<String> arguments) {
		this.executable = executable;
		this.arguments = arguments;
	}

	public String getExecutable() {
		return executable;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public static CommandLine of(String commandLine) {
		String[] commandLineWords = commandLine.split("\\s+");
		String executable = commandLineWords[0];

		List<String> arguments = Collections.emptyList();
		if (commandLineWords.length > 1) {
			arguments = Arrays.asList(Arrays.copyOfRange(commandLineWords, 1, commandLineWords.length));
		}

		return new CommandLine(executable, arguments);
	}
}
