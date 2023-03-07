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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.asGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.childNamed;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mainGroup;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.internal.testing.GradleRunnerMatchers.upToDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static org.hamcrest.MatcherAssert.assertThat;

class UpToDateCheckDetectsChangeToPBXResourcesBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	@Test
	void outOfDateWhenNewColorSet() throws IOException {
		writeColorSet(testDirectory.resolve("App/Assets.xcassets/NewColor.colorset"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}

	@Test
	void ignoresNewUnlinkedAssets() throws IOException {
		writeColorSet(testDirectory.resolve("App/OtherAssets.xcassets/NewColor.colorset"));
		mutateProject(mainGroup(childNamed("App", asGroup(children(add(ofGroup("OtherAssets.xcassets"))))))).accept(testDirectory.resolve("UpToDateCheck.xcodeproj"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), upToDate());
	}

	// TODO: Modifying storyboard should make this out-of-date but it doesn't work because how we calculate paths for PBXVariantGroup is broken
}
