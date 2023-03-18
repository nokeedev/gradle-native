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

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.clear;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.dependencies;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.files;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.childNameOrPath;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetDependencyTo;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXCopyFilesBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		xcodeproj(embedFrameworkIntoApp("Foo"));
		xcodeproj(targetUnderTest(dependencies(add(targetDependencyTo("Foo")))));

		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "App";
	}

	// TODO: modifying Common/Common.h should have no effect because it's not part of the final output... It is excluded by the built-in copy
	//    builtin-copy -exclude .DS_Store -exclude CVS -exclude .svn -exclude .git -exclude .hg -exclude Headers -exclude PrivateHeaders -exclude Modules -exclude \*.tbd -resolve-src-symlinks /Users/daniel/Library/Developer/Xcode/DerivedData/UpToDateCheck-cimdnpufgfbcnjaylgddbeqklcsy/Build/Products/Debug/Common.framework /Users/daniel/Library/Developer/Xcode/DerivedData/UpToDateCheck-cimdnpufgfbcnjaylgddbeqklcsy/Build/Products/Debug/App.app/Contents/Frameworks
	//   It seems to be ATTRIBUTES = (RemoveHeadersOnCopy)
	//   Not sure if this is the norm or just specific to this case...
	//   Note that if PBXFrameworksBuildPhase points to the framework, it will snapshot the entire Common.framework

	@Nested
	class FilesField {
		@Test
		void outOfDateWhenCompiledFrameworkChanges() throws IOException {
			appendChangeToSwiftFile(file("Foo/Foo.swift"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileAdded() {
			xcodeproj(embedFrameworkIntoApp("Bar"));
			xcodeproj(targetUnderTest(dependencies(add(targetDependencyTo("Bar")))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileRemoved() {
			xcodeproj(targetUnderTest(copyFilesBuildPhases(files(clear()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileEntryDuplicated() {
			xcodeproj(embedFrameworkIntoApp("Foo"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenResolvedFileDuplicated() {
			xcodeproj(alternateBuiltProduct("Foo.framework"));
			xcodeproj(embedFrameworkIntoApp("alternate-Foo"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileOrderingChanged() {
			xcodeproj(embedFrameworkIntoApp("Bar"));
			xcodeproj(targetUnderTest(dependencies(add(targetDependencyTo("Bar")))));

			ensureUpToDate(executer);

			xcodeproj(targetUnderTest(copyFilesBuildPhases(files(shuffleOrdering()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Disabled("outputs are not yet tracked")
	@Test
	void outOfDateWhenInputCopyFileRemoved() throws IOException {
		delete(appDebugProductsDirectory().resolve("App.app/Contents/Frameworks/Foo.framework/Foo"));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	private UnaryOperator<PBXProject> embedFrameworkIntoApp(String frameworkName) {
		return targetUnderTest(copyFilesBuildPhases(files(add(framework(frameworkName)))));
	}

	private static Function<PBXProject, PBXBuildFile> framework(String frameworkName) {
		return framework(frameworkName, builder -> builder.settings(ImmutableMap.of("ATTRIBUTES", of("CodeSignOnCopy", "RemoveHeadersOnCopy"))));
	}

	private static Function<PBXProject, PBXBuildFile> framework(String frameworkName, Consumer<? super PBXBuildFile.Builder> action) {
		assert !frameworkName.endsWith(".framework");
		return project -> {
			val productsGroup = (PBXGroup) project.getMainGroup().getChildren().stream().filter(childNameOrPath("Products")).collect(onlyElement());
			val frameworkFile = (PBXFileReference) productsGroup.getChildren().stream().filter(childNameOrPath(frameworkName + ".framework")).collect(onlyElement());

			val builder = PBXBuildFile.builder();
			action.accept(builder);
			return builder.fileRef(frameworkFile).build();
		};
	}
}
