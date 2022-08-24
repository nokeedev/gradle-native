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
import dev.nokee.xcode.objects.files.PBXReferenceProxy;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;

@ExtendWith(TestDirectoryExtension.class)
class PBXContainerItemProxyIntegrationTest {
	@TestDirectory Path testDirectory;

	@Test
	void crossProjectTargetReference() throws IOException {
		new CrossProjectReference().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("CrossProjectReference.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());

			assertThat(project.getTargets(), hasSize(1));
			assertThat(project.getTargets().get(0).getDependencies(), hasSize(1));

			val targetProxy = project.getTargets().get(0).getDependencies().get(0).getTargetProxy();
			assertThat(targetProxy.getContainerPortal(), isA(PBXFileReference.class));
			assertThat(targetProxy.getProxyType(), equalTo(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE));

			try (val libReader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("Library.xcodeproj/project.pbxproj"))))) {
				val proj = libReader.read();
				val libTarget = proj.getObjects().get("PBXNativeTarget").findFirst().get();
				assertThat(targetProxy.getRemoteGlobalIDString(), equalTo(libTarget.getGlobalID()));
			}
		}
	}

	@Test
	void selfProjectTargetReference() throws IOException {
		new GreeterAppWithLib().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("GreeterAppWithLib.xcodeproj/project.pbxproj"))))) {
			val proj = reader.read();
			val project = new PBXObjectUnarchiver().decode(proj);

			assertThat(project.getTargets(), hasSize(2));
			assertThat(project.getTargets().get(0).getDependencies(), hasSize(1));
			val targetProxy = project.getTargets().get(0).getDependencies().get(0).getTargetProxy();
			assertThat(targetProxy.getContainerPortal(), isA(PBXProject.class));
			assertThat(targetProxy.getProxyType(), equalTo(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE));

			val libTarget = proj.getObjects().get("PBXNativeTarget").collect(Collectors.toList()).get(1);
			assertThat(targetProxy.getRemoteGlobalIDString(), equalTo(libTarget.getGlobalID()));
		}
	}

	@Test
	void crossProjectProductReference() throws IOException {
		new CrossProjectReference().writeToProject(testDirectory.toFile());
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("CrossProjectReference.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());

			assertThat(project.getProjectReferences(), hasSize(1));
			assertThat(project.getProjectReferences().get(0).getProductGroup().getChildren(), hasSize(1));
			assertThat(project.getProjectReferences().get(0).getProductGroup().getChildren().get(0), isA(PBXReferenceProxy.class));

			val referenceProxy = ((PBXReferenceProxy) project.getProjectReferences().get(0).getProductGroup().getChildren().get(0)).getRemoteReference();
			assertThat(referenceProxy.getContainerPortal(), isA(PBXFileReference.class));
			assertThat(referenceProxy.getProxyType(), equalTo(PBXContainerItemProxy.ProxyType.REFERENCE));
			try (val libReader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("Library.xcodeproj/project.pbxproj"))))) {
				val proj = libReader.read();
				val libFile = proj.getObjects().get("PBXFileReference").collect(Collectors.toList()).get(2);
				assertThat(referenceProxy.getRemoteGlobalIDString(), equalTo(libFile.getGlobalID()));
			}
		}
	}
}
