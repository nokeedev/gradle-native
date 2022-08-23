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
package dev.nokee.xcode.project;

import dev.nokee.samples.xcode.GreeterAppWithLib;
import dev.nokee.samples.xcode.GreeterAppWithSwiftPackageReference;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.objects.buildphase.PBXFrameworksBuildPhase;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.xcode.objects.files.PBXFileReference.builder;
import static dev.nokee.xcode.objects.files.PBXSourceTree.BUILT_PRODUCTS_DIR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(TestDirectoryExtension.class)
class PBXBuildFileIntegrationTest {
	@TestDirectory Path testDirectory;

	@Test
	void linksAgainstSwiftPackageFramework() throws IOException {
		new GreeterAppWithSwiftPackageReference().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("GreeterAppWithSwiftPackageReference.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());

			assertThat(project.getTargets(), hasSize(1));

			val target = (PBXNativeTarget) project.getTargets().get(0);
			assertThat(target.getBuildPhases(), hasSize(3));

			val buildPhase = project.getTargets().get(0).getBuildPhases().stream().filter(PBXFrameworksBuildPhase.class::isInstance).findFirst().orElseThrow(RuntimeException::new);
			assertThat(buildPhase.getFiles(), hasSize(1));

			val subject = buildPhase.getFiles().get(0);
			assertThat(subject.getFileRef(), emptyOptional());
			assertThat(subject.getProductRef(), optionalWithValue(equalTo(target.getPackageProductDependencies().get(0))));
		}
	}

	@Test
	void linksAgainstLocalTargetFramework() throws IOException {
		new GreeterAppWithLib().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("GreeterAppWithLib.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());

			assertThat(project.getTargets(), hasSize(2));

			val target = (PBXNativeTarget) project.getTargets().get(0);
			assertThat(target.getBuildPhases(), hasSize(3));

			val buildPhase = project.getTargets().get(0).getBuildPhases().stream().filter(PBXFrameworksBuildPhase.class::isInstance).findFirst().orElseThrow(RuntimeException::new);
			assertThat(buildPhase.getFiles(), hasSize(1));

			val subject = buildPhase.getFiles().get(0);
			assertThat(subject.getFileRef(), optionalWithValue(equalTo(builder().path("libGreeterLib.a").sourceTree(BUILT_PRODUCTS_DIR).build())));
			assertThat(subject.getProductRef(), emptyOptional());
		}
	}
}
