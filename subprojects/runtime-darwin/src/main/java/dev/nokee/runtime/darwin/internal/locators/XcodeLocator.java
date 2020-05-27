package dev.nokee.runtime.darwin.internal.locators;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.google.common.collect.ImmutableSet;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.runtime.base.internal.tools.CommandLineToolDescriptor;
import dev.nokee.runtime.base.internal.tools.CommandLineToolLocator;
import dev.nokee.runtime.base.internal.tools.DefaultCommandLineToolDescriptor;
import org.gradle.util.VersionNumber;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Set;

import static dev.nokee.runtime.darwin.internal.locators.XcodeUtils.findTool;

public class XcodeLocator implements CommandLineToolLocator {
	public Set<CommandLineToolDescriptor> findAll(String toolName) {
		switch (toolName) {
			case "ibtool":
				return ImmutableSet.of(findInterfaceBuilderTool());
			case "codesign":
				return ImmutableSet.of(findCodeSignatureTool());
			case "actool":
				return ImmutableSet.of(findAssetCompilerTool());
			default:
				throw new UnsupportedOperationException("Doesn't know tool...");
		}
	}

	public Set<String> getKnownTools() {
		return ImmutableSet.of("ibtool", "codesign", "actool");
	}

	public CommandLineToolDescriptor findInterfaceBuilderTool() {
		File tool = findTool("ibtool");
		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVersion("com.apple.ibtool.version"));
		return new DefaultCommandLineToolDescriptor(tool, version.toString());
	}

	public CommandLineToolDescriptor findAssetCompilerTool() {
		File tool = findTool("actool");
		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVersion("com.apple.actool.version"));
		return new DefaultCommandLineToolDescriptor(tool, version.toString());
	}

	public CommandLineToolDescriptor findCodeSignatureTool() {
		File tool = findTool("codesign");
		VersionNumber version = VersionNumber.parse("11.3.1");
		return new DefaultCommandLineToolDescriptor(tool, version.toString());
	}

	private static CommandLineToolOutputParser<VersionNumber> asVersion(String toolKey) {
		return content -> {
			try {
				NSObject obj = PropertyListParser.parse(content.getBytes());
				if (!(obj instanceof NSDictionary)) {
					throw new RuntimeException();
				}
				NSDictionary dict = (NSDictionary) obj;
				obj = dict.get(toolKey);
				if (!(obj instanceof NSDictionary)) {
					throw new RuntimeException();
				}
				dict = (NSDictionary) obj;

				return VersionNumber.parse(dict.get("short-bundle-version").toJavaObject(String.class));

			} catch (ParserConfigurationException | ParseException | SAXException | PropertyListFormatException | IOException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
