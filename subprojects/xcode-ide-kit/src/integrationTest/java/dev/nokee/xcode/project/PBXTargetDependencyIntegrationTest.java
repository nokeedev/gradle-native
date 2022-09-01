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

import dev.nokee.samples.xcode.CrossProjectReference;
import dev.nokee.samples.xcode.GreeterAppWithLib;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.files.PBXFileReference;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;

@ExtendWith(TestDirectoryExtension.class)
class PBXTargetDependencyIntegrationTest {
	@TestDirectory Path testDirectory;

	@Test
	void crossProjectTargetDependency() throws IOException {
		new CrossProjectReference().writeToProject(testDirectory);
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("CrossProjectReference.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());

			assertThat(project.getTargets(), hasSize(1));
			assertThat(project.getTargets().get(0).getDependencies(), hasSize(1));

			val subject = project.getTargets().get(0).getDependencies().get(0);
			assertThat("usually use the remote target's name", subject.getName(), optionalWithValue(equalTo("GreeterLib")));
			assertThat("cross-project target dependency has no target", subject.getTarget(), emptyOptional());

			val targetProxy = subject.getTargetProxy();
			assertThat("reference a Xcode project wrapper", targetProxy.getContainerPortal(), isA(PBXFileReference.class));
			assertThat("is a target reference", targetProxy.getProxyType(), equalTo(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE));
		}
	}

	@Test
	void selfProjectTargetDependency() throws IOException {
		new GreeterAppWithLib().writeToProject(testDirectory);
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("GreeterAppWithLib.xcodeproj/project.pbxproj"))))) {
			val proj = reader.read();
			val project = new PBXObjectUnarchiver().decode(proj);

			assertThat(project.getTargets(), hasSize(2));
			assertThat(project.getTargets().get(0).getDependencies(), hasSize(1));

			val subject = project.getTargets().get(0).getDependencies().get(0);
			assertThat("usually does not have a name", subject.getName(), emptyOptional());
			assertThat("local-project target reference", subject.getTarget(), optionalWithValue(equalTo(project.getTargets().get(1))));

			val targetProxy = subject.getTargetProxy();
			assertThat("reference the PBXProject", targetProxy.getContainerPortal(), isA(PBXProject.class));
			assertThat("is a target reference", targetProxy.getProxyType(), equalTo(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE));
		}
	}
}
