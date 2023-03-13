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
package dev.nokee.buildadapter.xcode.uptodate;

import dev.gradleplugins.runnerkit.GradleRunner;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.removeLast;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXAggregateTargetFunctionalTests extends UpToDateCheckSpec {
	@Override
	GradleRunner configure(GradleRunner runner) {
		return runner.withTasks("AggregateDebug");
	}

	@Nested
	class BuildPhasesField {
		@Test
		void outOfDateWhenNewBuildPhase() {
			mutateProject(targetNamed("Aggregate", buildPhases(add(PBXCopyFilesBuildPhase.builder().destination(it -> it.frameworks("")).build())))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

			assertThat(executer.build().task(":UpToDateCheck:AggregateDebug"), outOfDate());
		}

		@Test
		void outOfDateWhenRemoveBuildPhase() {
			mutateProject(targetNamed("Aggregate", buildPhases(removeLast()))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

			assertThat(executer.build().task(":UpToDateCheck:AggregateDebug"), outOfDate());
		}
	}
}
