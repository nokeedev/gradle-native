/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.nokee.core.exec.CommandLineTool;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.project.PBXObjectArchiver;
import dev.nokee.xcode.project.PBXProjWriter;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static dev.nokee.core.exec.CommandLineToolExecutionEngine.processBuilder;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;

@EnabledOnOs(OS.MAC)
@ExtendWith(TestDirectoryExtension.class)
class MacroExpansionXcodeIntegrationTests extends MacroExpansionIntegrationTester {
	@TestDirectory Path testDirectory;

	@Override
	public String expand(String str) {
		val project = PBXProject.builder().buildConfigurations(it -> it.buildConfiguration(builder -> builder.name("TestCase").buildSettings(s -> {
			buildSettings().forEach(s::put);
			s.put("TestCase", str);
		}))).target(PBXAggregateTarget.builder().name("Test").buildConfigurations(it -> it.buildConfiguration(builder -> builder.name("TestCase"))).build()).build();

		try {
			Files.createDirectories(testDirectory.resolve("MyTest.xcodeproj"));
			try (val writer = new PBXProjWriter(Files.newBufferedWriter(testDirectory.resolve("MyTest.xcodeproj/project.pbxproj")))) {
				writer.write(new PBXObjectArchiver().encode(project));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		val buildSettings = CommandLineTool.of("xcodebuild").withArguments(it -> {
				it.args("-project", testDirectory.resolve("MyTest.xcodeproj"), "-target", "Test", "-configuration", "TestCase");
				it.args("-showBuildSettings", "-json");
			}).newInvocation(it -> {
				it.withEnvironmentVariables(inherit().putOrReplace("DEVELOPER_DIR", "/Applications/Xcode.app/Contents/Developer"));
				it.workingDirectory(testDirectory);
			}).submitTo(processBuilder()).waitFor()
			.getStandardOutput().parse(output -> {
				if (output.trim().isEmpty()) {
					throw new RuntimeException("Xcode crashed");
				} else {
					@SuppressWarnings("unchecked")
					val parsedOutput = (List<ShowBuildSettingsEntry>) new Gson().fromJson(output, new TypeToken<List<ShowBuildSettingsEntry>>() {}.getType());
					System.out.println("===");
					parsedOutput.get(0).getBuildSettings().entrySet().stream().filter(it -> buildSettings().containsKey(it.getKey()) || it.getKey().equals("TestCase")).forEach(it -> System.out.println("'" + it.getKey() + "' ==> '" + it.getValue() + "'"));
					System.out.println("===");
					return parsedOutput.get(0).getBuildSettings();
				}
			});

		return buildSettings.get("TestCase");
	}

	private static final class ShowBuildSettingsEntry {
		private final Map<String, String> buildSettings;

		private ShowBuildSettingsEntry(Map<String, String> buildSettings) {
			this.buildSettings = buildSettings;
		}

		public Map<String, String> getBuildSettings() {
			return buildSettings;
		}
	}
}
