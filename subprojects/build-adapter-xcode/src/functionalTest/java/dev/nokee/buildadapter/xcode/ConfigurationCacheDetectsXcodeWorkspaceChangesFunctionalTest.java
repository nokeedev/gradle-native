/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode;

import dev.gradleplugins.runnerkit.BuildResult;
import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import dev.nokee.internal.testing.junit.jupiter.GradleFeatureRequirement;
import dev.nokee.internal.testing.junit.jupiter.RequiresGradleFeature;
import dev.nokee.platform.xcode.EmptyXCProject;
import dev.nokee.platform.xcode.EmptyXCWorkspace;
import dev.nokee.xcode.workspace.XCFileReference;
import dev.nokee.xcode.workspace.XCWorkspaceData;
import dev.nokee.xcode.workspace.XCWorkspaceDataWriter;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.buildadapter.xcode.GradleTestSnippets.doSomethingVerifyTask;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RequiresGradleFeature(GradleFeatureRequirement.CONFIGURATION_CACHE)
@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class ConfigurationCacheDetectsXcodeWorkspaceChangesFunctionalTest {
	GradleRunner executer;
	@TestDirectory Path testDirectory;
	BuildResult result;

	@BeforeEach
	void setup(GradleRunner runner) throws IOException {
		new EmptyXCWorkspace("Test").writeToProject(testDirectory);
		Files.createDirectories(testDirectory.resolve("Test.xcworkspace/xcuserdata/foo.xcuserdatad"));
		Files.write(testDirectory.resolve("Test.xcworkspace/xcuserdata/foo.xcuserdatad/UserInterfaceState.xcuserstate"), new byte[] {0xF, 0x0, 0x0}, StandardOpenOption.CREATE_NEW);
		doSomethingVerifyTask().writeTo(testDirectory.resolve("build.gradle"));
		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withArgument("verify").withArgument("--configuration-cache");
		result = executer.build();
	}

	@Test
	void reuseConfigurationCacheWhenOnlyUserDataChanged() throws IOException {
		Files.write(testDirectory.resolve("Test.xcworkspace/xcuserdata/foo.xcuserdatad/UserInterfaceState.xcuserstate"), new byte[] {0xB, 0x0, 0xB}, StandardOpenOption.APPEND);
		assertThat(executer.build().getOutput(), containsString("Reusing configuration cache"));
	}

	@Test
	void reuseConfigurationCacheWhenWorkspaceContentChangeInNonMeaningfulWay() throws IOException {
		// We serialize the workspace model hence, any change that doesn't change the model will be no-op.
		Files.write(testDirectory.resolve("Test.xcworkspace/contents.xcworkspacedata"), Collections.singletonList(""), StandardOpenOption.APPEND);
		assertThat(executer.build().getOutput(), containsString("Reusing configuration cache"));
	}

	@Test
	void doesNotReuseConfigurationCacheWhenWorkspaceContentChange() throws IOException {
		try (val writer = new XCWorkspaceDataWriter(Files.newBufferedWriter(testDirectory.resolve("Test.xcworkspace/contents.xcworkspacedata")))) {
			writer.write(XCWorkspaceData.builder().fileRef(XCFileReference.of("group:Pods/Pods.xcodeproj")).build());
		}
		new EmptyXCProject("Pods").writeToProject(testDirectory.resolve("Pods"));
		assertThat(executer.build().getOutput(), not(containsString("Reusing configuration cache")));
	}

	@Test
	void doesNotReuseConfigurationCacheWhenNewWorkspaceFound() {
		new EmptyXCWorkspace("App").writeToProject(testDirectory);
		assertThat(executer.build().getOutput(), not(containsString("Reusing configuration cache")));
	}
}
