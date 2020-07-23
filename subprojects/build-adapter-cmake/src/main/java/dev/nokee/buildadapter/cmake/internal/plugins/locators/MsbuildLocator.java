package dev.nokee.buildadapter.cmake.internal.plugins.locators;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.DefaultCommandLineToolDescriptor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class MsbuildLocator implements CommandLineToolLocator {
	private final Supplier<CommandLineTool> vswhere;

	@Override
	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		val tool = vswhere.get().withArguments("-latest", "-requires", "Microsoft.Component.MSBuild", "-find", "MSBuild\\**\\Bin\\MSBuild.exe").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asPath());
		val version = CommandLineTool.of(tool).withArguments("-version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asMsbuildVersion());
		return Collections.singleton(new DefaultCommandLineToolDescriptor(tool, version.toString()));
	}

	@Override
	public Set<String> getKnownTools() {
		return Collections.singleton("msbuild");
	}

	private static CommandLineToolOutputParser<File> asPath() {
		return content -> new File(content.trim());
	}

	private static final Pattern MSBUILD_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+.\\d+(.\\d+)?)");
	static CommandLineToolOutputParser<VersionNumber> asMsbuildVersion() {
		return content -> {
			Matcher matcher = MSBUILD_VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return VersionNumber.parse(matcher.group(1));
			}
			// TODO: Print better error message
			throw new RuntimeException("Invalid version");
		};
	}
}
