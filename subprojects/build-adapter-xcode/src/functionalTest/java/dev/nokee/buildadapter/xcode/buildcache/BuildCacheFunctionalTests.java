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
package dev.nokee.buildadapter.xcode.buildcache;

import dev.gradleplugins.runnerkit.GradleRunner;
import dev.gradleplugins.runnerkit.TaskOutcome;
import dev.nokee.UpToDateCheck;
import dev.nokee.internal.testing.junit.jupiter.ContextualGradleRunnerParameterResolver;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.PluginsBlock.plugins;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildFileToProduct;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.copyFilesBuildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.dependencies;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.files;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetDependencyTo;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outcome;
import static dev.nokee.internal.testing.GradleRunnerMatchers.skipped;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;

@ExtendWith({TestDirectoryExtension.class, ContextualGradleRunnerParameterResolver.class})
class BuildCacheFunctionalTests {
	GradleRunner executer;
	@TestDirectory Path testDirectory;

	@BeforeEach
	void given(GradleRunner runner) throws IOException {
		new UpToDateCheck().writeToProject(testDirectory);
		mutateProject(targetNamed("App", copyFilesBuildPhases(files(add(buildFileToProduct("Foo.framework")))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
		mutateProject(targetNamed("App", copyFilesBuildPhases(files(add(buildFileToProduct("Bar.framework")))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
		mutateProject(targetNamed("App", dependencies(add(targetDependencyTo("Foo"))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));
		mutateProject(targetNamed("App", dependencies(add(targetDependencyTo("Bar"))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		plugins(it -> it.id("dev.nokee.xcode-build-adapter")).writeTo(testDirectory.resolve("settings.gradle"));
		executer = runner.withTasks("AppDebug").withArgument("-Dsdk=macosx").withBuildCacheEnabled();

		ensureUpToDate(executer);
	}

	@Test
	void restoreTargetOutputFromCache(GradleRunner runner) {
		runner.withTasks("clean").build();

		val result = executer.build();

		assertThat(result.task(":UpToDateCheck:FooDebug"), outcome(equalTo(TaskOutcome.FROM_CACHE)));
		assertThat(result.task(":UpToDateCheck:BarDebug"), outcome(equalTo(TaskOutcome.FROM_CACHE)));
		assertThat(result.task(":UpToDateCheck:AppDebug"), outcome(equalTo(TaskOutcome.FROM_CACHE)));
	}

	private static void ensureUpToDate(GradleRunner executer) {
		executer.build();
		assertThat(executer.build().getTasks(), everyItem(skipped()));
	}
}
