package dev.nokee.runtime.nativebase.internal.locators;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.DefaultCommandLineToolDescriptor;
import lombok.val;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VswhereLocator implements CommandLineToolLocator {
	@Override
	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		val tool = new File(getProgramFileX86() + "/Microsoft Visual Studio/Installer/vswhere.exe");
		if (tool.exists()) {
			VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVswhereVersion());
			return Collections.singleton(new DefaultCommandLineToolDescriptor(tool, version.toString()));
		}
		return Collections.emptySet();
	}

	private String getProgramFileX86() {
		val result = System.getenv("ProgramFiles(x86)");
//		Preconditions.checkNotNull(result);
		return result;
	}

	@Override
	public Set<String> getKnownTools() {
		return Collections.singleton("vswhere");
	}

	private static final Pattern VSWHERE_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?)");
	static CommandLineToolOutputParser<VersionNumber> asVswhereVersion() {
		return content -> {
			Matcher matcher = VSWHERE_VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return VersionNumber.parse(matcher.group(1));
			}
			// TODO: Print better error message
			throw new RuntimeException("Invalid version");
		};
	}
}
