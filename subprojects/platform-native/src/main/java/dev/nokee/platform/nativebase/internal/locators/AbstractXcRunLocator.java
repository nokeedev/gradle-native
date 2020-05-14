package dev.nokee.platform.nativebase.internal.locators;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.ProcessBuilderEngine;

import java.io.File;

public abstract class AbstractXcRunLocator implements XcRunLocator {
	protected abstract String getXcRunFlagPrefix();

	public File findPath() {
		return new File(xcrun(getXcRunFlagPrefix() + "-path"));
	}

	@Override
	public String findVersion() {
		return xcrun(getXcRunFlagPrefix() + "-version");
	}

	private String xcrun(String flag) {
		return CommandLineTool.of("xcrun").withArguments(flag).execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim();
	}
}
