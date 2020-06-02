package dev.nokee.runtime.darwin.internal.locators;

import com.google.common.collect.ImmutableSet;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.DefaultCommandLineToolDescriptor;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.nokee.runtime.darwin.internal.locators.XcodeUtils.findTool;

public class XcodebuildLocator implements CommandLineToolLocator {
	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		File tool = findTool("xcodebuild");
		VersionNumber version = CommandLineTool.of(tool).withArguments("-version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asXcodeRunVersion());
		return ImmutableSet.of(new DefaultCommandLineToolDescriptor(tool, version.toString()));
	}

	public Set<String> getKnownTools() {
		return ImmutableSet.of("xcodebuild");
	}

	private static final Pattern XCODEBUILD_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?)");
	private static CommandLineToolOutputParser<VersionNumber> asXcodeRunVersion() {
		return content -> {
			Matcher matcher = XCODEBUILD_VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return VersionNumber.parse(matcher.group(1));
			}
			// TODO: Print better error message
			throw new RuntimeException("Invalid version");
		};
	}
}
