package dev.nokee.platform.nativebase.internal.repositories;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.core.exec.CommandLineToolOutputParser;
import dev.nokee.core.exec.ProcessBuilderEngine;
import org.gradle.util.VersionNumber;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import static java.util.Arrays.asList;

public class XcodeLocator {
	public List<ToolDescriptor> findAll() {
		return asList(findInterfaceBuilderTool(), findCodeSignatureTool(), findAssetCompilerTool());
	}

	public ToolDescriptor findInterfaceBuilderTool() {
		File tool = findTool("ibtool");
		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVersion("com.apple.ibtool.version"));
		return new DefaultToolDescriptor(tool, version.toString());
	}

	public ToolDescriptor findAssetCompilerTool() {
		File tool = findTool("actool");
		VersionNumber version = CommandLineTool.of(tool).withArguments("--version").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asVersion("com.apple.actool.version"));
		return new DefaultToolDescriptor(tool, version.toString());
	}

	public ToolDescriptor findCodeSignatureTool() {
		File tool = findTool("codesign");
		VersionNumber version = VersionNumber.parse("11.3.1");
		return new DefaultToolDescriptor(tool, version.toString());
	}

	public File findTool(String name) {
		return CommandLineTool.fromPath("xcrun").withArguments("--find", name).newInvocation().buildAndSubmit(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().parse(asPath());
	}

	private static CommandLineToolOutputParser<File> asPath() {
		return content -> new File(content.trim());
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
