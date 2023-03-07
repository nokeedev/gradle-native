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

import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.buildPhases;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.childName;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.dependencies;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.mutateProject;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.nameOrPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetDependencyTo;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

class UpToDateCheckDetectsChangeToPBXCopyFilesBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	void setup(Path location) {
		embedCommonFrameworkIntoApp().accept(location);
	}

	// TODO: modifying Common/Common.h should have no effect because it's not part of the final output... It is excluded by the built-in copy
	//    builtin-copy -exclude .DS_Store -exclude CVS -exclude .svn -exclude .git -exclude .hg -exclude Headers -exclude PrivateHeaders -exclude Modules -exclude \*.tbd -resolve-src-symlinks /Users/daniel/Library/Developer/Xcode/DerivedData/UpToDateCheck-cimdnpufgfbcnjaylgddbeqklcsy/Build/Products/Debug/Common.framework /Users/daniel/Library/Developer/Xcode/DerivedData/UpToDateCheck-cimdnpufgfbcnjaylgddbeqklcsy/Build/Products/Debug/App.app/Contents/Frameworks
	//   Not sure if this is the norm or just specific to this case...
	//   Note that if PBXFrameworksBuildPhase points to the framework, it will snapshot the entire Common.framework

	@Test
	void outOfDateWhenCompiledFrameworkChange() throws IOException {
		appendMeaningfulChangeToCFile(testDirectory.resolve("Common/Common.c"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}

	@Disabled("outputs are not yet tracked")
	@Test
	void outOfDateWhenInputCopyFileRemoved() throws IOException {
		delete(appDebugProductsDirectory().resolve("App.app/Contents/Frameworks/Common.framework/Common"));

		assertThat(executer.build().task(":UpToDateCheck:AppDebug"), outOfDate());
	}

	private static Consumer<Path> embedCommonFrameworkIntoApp() {
		return mutateProject(project -> {
			val productsGroup = (PBXGroup) project.getMainGroup().getChildren().stream().filter(childName("Products")).collect(onlyElement());
			val commonFrameworkFile = (PBXFileReference) productsGroup.getChildren().stream().filter(nameOrPath("Common.framework")).collect(onlyElement());
			val newProject = targetNamed("App", buildPhases(add(PBXCopyFilesBuildPhase.builder().dstSubfolderSpec(PBXCopyFilesBuildPhase.SubFolder.Frameworks).dstPath("").file(PBXBuildFile.ofFile(commonFrameworkFile)).build()))).apply(project);
			return targetNamed("App", dependencies(add(targetDependencyTo("Common")))).apply(newProject);
		});
	}
}
