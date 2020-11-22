package dev.gradleplugins.exemplarkit.asciidoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class AsciidocCommandLineParser {
	private final String executable;
	private final List<String> arguments;

	private AsciidocCommandLineParser(String executable, List<String> arguments) {
		this.executable = executable;
		this.arguments = arguments;
	}

	public String getExecutable() {
		return executable;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public static AsciidocCommandLineParser parse(String commandLine) {
		String[] commandLineWords = commandLine.split("\\s+");
		String executable = commandLineWords[0];

		List<String> arguments = Collections.emptyList();
		if (commandLineWords.length > 1) {
			arguments = Arrays.asList(Arrays.copyOfRange(commandLineWords, 1, commandLineWords.length));
		}

		return new AsciidocCommandLineParser(executable, arguments);
	}
}
