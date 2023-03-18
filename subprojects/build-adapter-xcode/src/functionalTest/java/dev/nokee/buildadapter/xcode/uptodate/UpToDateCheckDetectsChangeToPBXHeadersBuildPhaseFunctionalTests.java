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
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.add;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.children;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.clear;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.dependencies;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.files;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetDependencyTo;
import static dev.nokee.buildadapter.xcode.PBXProjectTestUtils.targetNamed;
import static dev.nokee.internal.testing.GradleRunnerMatchers.outOfDate;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofGroup;
import static java.nio.file.Files.delete;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledOnOs(OS.MAC)
class UpToDateCheckDetectsChangeToPBXHeadersBuildPhaseFunctionalTests extends UpToDateCheckSpec {
	@BeforeEach
	void setup() throws IOException {
		xcodeproj(targetNamed("App", dependencies(add(targetDependencyTo(targetUnderTestName())))));

		executer = executer.withTasks("AppDebug"); // ensure the App is built which depends on the target under test

		ensureUpToDate(executer);
	}

	@Override
	protected String targetUnderTestName() {
		return "Foo";
	}

	@Nested
	class FilesField {
		@Test // TODO: check here
		void outOfDateWhenPrivateHeaderChange() throws IOException {
			appendChangeToCHeader(file("Foo/Foo.h"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test // TODO: What about public/protected
		void outOfDateWhenPublicHeaderFileAdded() throws IOException {
			xcodeproj(groupUnderTest(children(add(ofGroup("MyApp.h")))));
			xcodeproj(targetUnderTest(headersBuildPhases(files(add(buildFileTo("MyApp.h", asPublic()))))));
			Files.write(file("Foo/MyApp.h"), Arrays.asList("// my app header"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test // TODO: What about public/protected
		void outOfDateWhenPrivateHeaderFileAdded() throws IOException {
			xcodeproj(groupUnderTest(children(add(ofGroup("MyApp.h")))));
			xcodeproj(targetUnderTest(headersBuildPhases(files(add(buildFileTo("MyApp.h", asPrivate()))))));
			Files.write(file("Foo/MyApp.h"), Arrays.asList("// my app header"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test // TODO: What about public/protected
		void outOfDateWhenProjectHeaderFileAdded() throws IOException {
			xcodeproj(groupUnderTest(children(add(ofGroup("MyApp.h")))));
			xcodeproj(targetUnderTest(headersBuildPhases(files(add(buildFileTo("MyApp.h", asProject()))))));
			Files.write(file("Foo/MyApp.h"), Arrays.asList("// my app header"));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileRemoved() {
			xcodeproj(targetUnderTest(headersBuildPhases(files(clear()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenFileEntryDuplicated() {
			xcodeproj(targetUnderTest(headersBuildPhases(files(add(buildFileTo("Foo.h", asPublic()))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenResolvedFileDuplicated() {
			xcodeproj(alternateFileUnderTest("Foo.h"));
			xcodeproj(targetUnderTest(headersBuildPhases(files(add(buildFileTo("alternate-Foo.h", asPublic()))))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}

		@Test
		void outOfDateWhenHeadersOrderingChanged() throws IOException {
			Files.write(file("Foo/MyApp.h"), Arrays.asList("// my app header"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			xcodeproj(groupUnderTest(children(add(ofGroup("MyApp.h")))));
			xcodeproj(targetUnderTest(headersBuildPhases(files(add(buildFileTo("MyApp.h", asPublic()))))));

			ensureUpToDate(executer);

			xcodeproj(targetUnderTest(headersBuildPhases(files(shuffleOrdering()))));

			assertThat(targetUnderTestExecution(), outOfDate());
		}
	}

	@Disabled("outputs are not yet tracked")
	@Test // TODO: This may actually trigger the productReference....
	void outOfDateWhenDeletePrivateHeaderFromFramework() throws IOException {
		delete(appDebugProductsDirectory().resolve("Foo.framework/Versions/A/Headers/Foo.h"));

		assertThat(targetUnderTestExecution(), outOfDate());
	}

	public static Consumer<PBXBuildFile.Builder> asPublic() {
		return it -> it.settings(ImmutableMap.of("ATTRIBUTES", of("Public")));
	}

	public static Consumer<PBXBuildFile.Builder> asPrivate() {
		return it -> it.settings(ImmutableMap.of("ATTRIBUTES", of("Private")));
	}

	public static Consumer<PBXBuildFile.Builder> asProject() {
		return it -> it.settings(ImmutableMap.of("ATTRIBUTES", of("Project")));
	}
}
