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
package dev.nokee.gradle;

import dev.nokee.publishing.internal.metadata.GradleModuleMetadata;
import lombok.val;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import static dev.nokee.gradle.AdhocArtifactRepositoryFactory.forProject;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.forId;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.forModule;
import static dev.nokee.gradle.AdhocArtifactRepositoryTestUtils.query;
import static dev.nokee.internal.testing.util.ProjectTestUtils.createRootProject;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static dev.nokee.publishing.internal.metadata.GradleModuleMetadata.Component.ofComponent;
import static java.nio.file.Files.readAllBytes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

@ExtendWith({MockitoExtension.class, TestDirectoryExtension.class})
class AdhocArtifactRepositoryResolutionIntegrationTest {
	@TestDirectory Path testDirectory;
	Project project;
	@Mock AdhocComponentSupplier supplier;
	@Mock AdhocComponentLister lister;

	@BeforeEach
	void setUp() {
		project = createRootProject(testDirectory);
		project.getRepositories().add(forProject(project).create());
		project.getRepositories().withType(AdhocArtifactRepository.class).configureEach(repo -> {
			repo.setName("test");
			repo.getCacheDirectory().set(project.getLayout().getBuildDirectory().dir("m2/test"));
			repo.setComponentSupplier(supplier);
			repo.setComponentVersionLister(lister);
		});
	}

	@Test
	void listsModuleVersionBeforeResolvingComponent() {
		Mockito.doAnswer(it -> {
			val details = it.getArgument(0, AdhocComponentListerDetails.class);
			details.listed(Arrays.asList("1.2.3"));
			return null;
		}).when(lister).execute(any());

		query(project, "com.example:foo:1.+");
		val inOrder = Mockito.inOrder(lister, supplier);
		inOrder.verify(lister).execute(argThat(forModule("com.example:foo")));
		inOrder.verify(supplier).execute(argThat(forId("com.example:foo:1.2.3")));
	}

	@Test
	void doesNotExecuteSupplierRuleWhenResolvingStaticComponent() {
		query(project, "com.example:foo:1.4");
		Mockito.verify(lister, never()).execute(any());
		Mockito.verify(supplier, only()).execute(argThat(forId("com.example:foo:1.4")));
	}


	@Test // can resolve dynamic list
	void canProvideModuleVersionsInAnyOrder() {
		Mockito.doAnswer(it -> {
			val details = it.getArgument(0, AdhocComponentListerDetails.class);
			details.listed(Arrays.asList("1.0", "1.2", "1.1.1", "1.1"));
			return null;
		}).when(lister).execute(any());

		query(project, "com.example:foo:1.+");
		val inOrder = Mockito.inOrder(lister, supplier);
		inOrder.verify(lister).execute(argThat(forModule("com.example:foo")));
		inOrder.verify(supplier).execute(argThat(forId("com.example:foo:1.2")));
	}

	@Test
	void canResolveComponentByVersion() throws IOException {
		Mockito.doAnswer(args -> {
			val details = args.getArgument(0, AdhocComponentSupplierDetails.class);
			details.metadata(builder -> {
				builder.component(ofComponent("com.example", "foo", "1.2"));
				builder.localVariant(it -> it.name("mainLink").attribute(ofAttribute("org.gradle.usage", "native-link")).file(GradleModuleMetadata.File.builder().name("some-file.txt").url("some-file.txt").build()));
			});
			details.file("some-file.txt", writer -> {
				try {
					writer.write("hey, listen!".getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			return null;
		}).when(supplier).execute(any());

		assertThat(new String(readAllBytes(query(project, "com.example:foo:1.2").getSingleFile().toPath())),
			equalTo("hey, listen!"));

		Mockito.verify(supplier).execute(argThat(forId("com.example:foo:1.2")));
	}
}
