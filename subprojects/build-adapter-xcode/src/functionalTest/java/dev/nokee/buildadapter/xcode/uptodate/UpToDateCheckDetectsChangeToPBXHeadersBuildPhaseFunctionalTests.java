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

import dev.gradleplugins.runnerkit.TaskOutcome;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.dependencies;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetDependencyTo;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class UpToDateCheckDetectsChangeToPBXHeadersBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	void setup(Path location) {
		mutateProject(targetNamed("App", dependencies(add(targetDependencyTo("Common"))))).accept(location);
	}

	@Test
	void outOfDateWhenPrivateHeaderChange() throws IOException {
		appendChangeToCHeader(testDirectory.resolve("Common/Common.h"));

		assertThat(executer.build().task(":UpToDateCheck:CommonDebug"), outOfDate());
	}

	@Disabled("outputs are not yet tracked")
	@Test // TODO: This may actually trigger the productReference....
	void outOfDateWhenDeletePrivateHeaderFromFramework() throws IOException {
		delete(appDebugProductsDirectory().resolve("Common.framework/Versions/A/Headers/Common.h"));

		assertThat(executer.build().task(":UpToDateCheck:CommonDebug").getOutcome(), equalTo(TaskOutcome.SUCCESS));
	}
}
