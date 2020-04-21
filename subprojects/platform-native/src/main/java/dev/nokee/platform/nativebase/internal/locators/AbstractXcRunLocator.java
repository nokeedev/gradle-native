package dev.nokee.platform.nativebase.internal.locators;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

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
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		CommandLine commandLine = new CommandLine("xcrun").addArgument(flag);
		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(new File(System.getProperty("user.dir")));
		executor.setStreamHandler(new PumpStreamHandler(outputStream));
		try {
			executor.execute(commandLine, System.getenv());
		} catch (IOException e) {
			throw new UncheckedIOException("Fail to execute xcrun:\n" + outputStream.toString(), e);
		}
		return outputStream.toString().trim();
	}
}
