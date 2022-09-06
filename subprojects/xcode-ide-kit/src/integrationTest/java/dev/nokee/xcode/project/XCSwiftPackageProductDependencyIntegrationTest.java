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

import dev.nokee.samples.xcode.GreeterAppWithSwiftPackageReference;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMajorVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;

@ExtendWith(TestDirectoryExtension.class)
class XCSwiftPackageProductDependencyIntegrationTest {
	@TestDirectory Path testDirectory;

	@Test
	void readsSwiftPackageProductDependencies() throws IOException {
		new GreeterAppWithSwiftPackageReference().writeToProject(testDirectory);
		try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(testDirectory.resolve("GreeterApp.xcodeproj/project.pbxproj"))))) {
			val project = new PBXObjectUnarchiver().decode(reader.read());

			assertThat(project.getTargets(), hasSize(1));
			assertThat(project.getTargets().get(0), isA(PBXNativeTarget.class));

			val subject = ((PBXNativeTarget) project.getTargets().get(0)).getPackageProductDependencies();
			assertThat(subject, hasSize(1));
			assertThat(subject.get(0).getProductName(), equalTo("o"));
			assertThat(subject.get(0).getPackageReference().getRepositoryUrl(), equalTo("https://github.com/0xOpenBytes/o.git"));
			assertThat(subject.get(0).getPackageReference().getRequirement(), equalTo(upToNextMajorVersion("0.2.1")));
		}
	}
}
