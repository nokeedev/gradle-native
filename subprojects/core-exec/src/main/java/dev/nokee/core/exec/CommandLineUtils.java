package dev.nokee.core.exec;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS;

final class CommandLineUtils {
	private CommandLineUtils() {}

	public static List<Object> getScriptCommandLine() {
		if (IS_OS_WINDOWS) {
			return Arrays.asList("cmd", "/c");
		}
		return Arrays.asList("/bin/bash", "-c");
	}
}
