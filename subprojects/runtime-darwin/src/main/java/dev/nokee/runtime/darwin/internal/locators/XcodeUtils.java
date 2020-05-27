package dev.nokee.runtime.darwin.internal.locators;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;

import java.io.File;

public class XcodeUtils {
	// TODO: We should use the xcrun already located
	public static File findTool(String name) {
		return CommandLineTool.fromPath("xcrun").withArguments("--find", name).newInvocation().buildAndSubmit(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asPath());
	}

	private static CommandLineToolOutputParser<File> asPath() {
		return content -> new File(content.trim());
	}
}
