package dev.nokee.buildadapter.cmake.internal.plugins.locators;

import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.DefaultCommandLineToolDescriptor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CmakeLocator implements CommandLineToolLocator {
	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		File tool = OperatingSystem.current().findInPath("cmake");
		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asCMakeVersion());
		return Collections.singleton(new DefaultCommandLineToolDescriptor(tool, version.toString()));
	}

	public Set<String> getKnownTools() {
		return Collections.singleton("cmake");
	}

	private static final Pattern CMAKE_VERSION_PATTERN = Pattern.compile("(\\d+.\\d+(.\\d+)?)");
	static CommandLineToolOutputParser<VersionNumber> asCMakeVersion() {
		return content -> {
			Matcher matcher = CMAKE_VERSION_PATTERN.matcher(content);
			if (matcher.find()) {
				return VersionNumber.parse(matcher.group(1));
			}
			// TODO: Print better error message
			throw new RuntimeException("Invalid version");
		};
	}

//	public CommandLineToolDescriptor findInterfaceBuilderTool() {
//		File tool = findTool("ibtool");
//		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVersion("com.apple.ibtool.version"));
//		return new DefaultCommandLineToolDescriptor(tool, version.toString());
//	}
//
//	public CommandLineToolDescriptor findAssetCompilerTool() {
//		File tool = findTool("actool");
//		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVersion("com.apple.actool.version"));
//		return new DefaultCommandLineToolDescriptor(tool, version.toString());
//	}
//
//	public CommandLineToolDescriptor findCodeSignatureTool() {
//		File tool = findTool("codesign");
//		VersionNumber version = VersionNumber.parse("11.3.1");
//		return new DefaultCommandLineToolDescriptor(tool, version.toString());
//	}

//	private static CommandLineToolOutputParser<VersionNumber> asVersion(String toolKey) {
//		return content -> {
//			try {
//				NSObject obj = PropertyListParser.parse(content.getBytes());
//				if (!(obj instanceof NSDictionary)) {
//					throw new RuntimeException();
//				}
//				NSDictionary dict = (NSDictionary) obj;
//				obj = dict.get(toolKey);
//				if (!(obj instanceof NSDictionary)) {
//					throw new RuntimeException();
//				}
//				dict = (NSDictionary) obj;
//
//				return VersionNumber.parse(dict.get("short-bundle-version").toJavaObject(String.class));
//
//			} catch (ParserConfigurationException | ParseException | SAXException | PropertyListFormatException | IOException e) {
//				throw new RuntimeException(e);
//			}
//		};
//	}
}
