/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.ios.tasks.internal;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.nio.charset.Charset;

public class ProcessPropertyListTask extends DefaultTask {
	private final ConfigurableFileCollection sources;
	private final Property<String> module;
	private final Property<String> identifier;
	private final RegularFileProperty outputFile;
	private final ExecOperations execOperations;

	@Optional
	@SkipWhenEmpty // TODO: Test no source
	@IgnoreEmptyDirectories
	@InputFiles
	public ConfigurableFileCollection getSources() {
		return sources;
	}

	@Input
	public Property<String> getModule() {
		return module;
	}

	@Input
	public Property<String> getIdentifier() {
		return identifier;
	}

	// TODO: Find a better name
	@OutputFile
	public RegularFileProperty getOutputFile() {
		return outputFile;
	}

	@Inject
	public ProcessPropertyListTask(ObjectFactory objects, ExecOperations execOperations) {
		this.sources = objects.fileCollection();
		this.module = objects.property(String.class);
		this.identifier = objects.property(String.class);
		this.outputFile = objects.fileProperty();
		this.execOperations = execOperations;
	}

	@TaskAction
	private void process() throws IOException {
		File xmlPlist = new File(getTemporaryDir(), "Info.plist");
		xmlPlist.delete();

		// Note: There seems to be a limit of how many command can be passed to the tool so we are using several invocation

		// Merge plist files
		execOperations.exec(spec -> {
			spec.executable(getPlistBuddyExecutable().getAbsolutePath());
			for (File source : getSources()) {
				spec.args("-c", "Merge \"" + source.getAbsolutePath() + "\"");
			}
			spec.args("-c", "Save");
			spec.args(xmlPlist.getAbsolutePath());
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt")));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});

		// Add information automatically added by Xcode, part 1
		execOperations.exec(spec -> {
			spec.executable(getPlistBuddyExecutable().getAbsolutePath());
			spec.args("-c", "Add :DTSDKName string iphonesimulator13.2");
			spec.args("-c", "Add :DTXcode string 1130");
			spec.args("-c", "Add :DTSDKBuild string 17B102");
			spec.args("-c", "Add :BuildMachineOSBuild string 19D76");
			spec.args("-c", "Add :DTPlatformName string iphonesimulator");
			spec.args("-c", "Add :CFBundleSupportedPlatforms array");
			spec.args("-c", "Add :CFBundleSupportedPlatforms:0 string iPhoneSimulator");
			spec.args("-c", "Add :DTCompiler string com.apple.compilers.llvm.clang.1_0");
			spec.args("-c", "Save");
			spec.args(xmlPlist.getAbsolutePath());
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"), true));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});

		// Add information automatically added by Xcode, part 2
		execOperations.exec(spec -> {
			spec.executable(getPlistBuddyExecutable().getAbsolutePath());
			spec.args("-c", "Add :MinimumOSVersion string 13.2");
			spec.args("-c", "Add :DTPlatformVersion string 13.2");
			spec.args("-c", "Add :UIDeviceFamily array");
			spec.args("-c", "Add :UIDeviceFamily:0 integer 1");
			spec.args("-c", "Add :UIDeviceFamily:1 integer 2");
			spec.args("-c", "Add :DTXcodeBuild string 11C504");
			spec.args("-c", "Add :DTPlatformBuild string");
			spec.args("-c", "Save");
			spec.args(xmlPlist.getAbsolutePath());
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"), true));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});

		// Ex: Product name: objective-c-ios-app
		// Ex: Product module name: objective_c_ios_app
		String data = FileUtils.readFileToString(xmlPlist, Charset.defaultCharset()).replace("$(PRODUCT_NAME)", getModule().get()).replace("$(EXECUTABLE_NAME)", getModule().get()).replace("$(PRODUCT_BUNDLE_IDENTIFIER)", getIdentifier().get()).replace("$(DEVELOPMENT_LANGUAGE)", "en").replace("$(PRODUCT_BUNDLE_PACKAGE_TYPE)", "APPL").replace("$(PRODUCT_MODULE_NAME)", getModule().get());
		FileUtils.write(xmlPlist, data, Charset.defaultCharset());

		execOperations.exec(spec -> {
			spec.executable(getPlutilExecutable().getAbsolutePath());
			spec.args("-convert", "binary1", "-o", getOutputFile().get().getAsFile().getAbsolutePath(), xmlPlist.getAbsolutePath());
			try {
				spec.setStandardOutput(new FileOutputStream(new File(getTemporaryDir(), "outputs.txt"), true));
			} catch (FileNotFoundException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@InputFile
	protected File getPlistBuddyExecutable() {
		return new File("/usr/libexec/PlistBuddy");
	}

	@InputFile
	protected File getPlutilExecutable() {
		return new File(getPlutilPath());
	}

	private static String getPlutilPath() {
		try {
			Process process = new ProcessBuilder("xcrun", "--sdk", "iphonesimulator", "--find", "plutil").start();
			process.waitFor();
			return IOUtils.toString(process.getInputStream(), Charset.defaultCharset()).trim();
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}
}
