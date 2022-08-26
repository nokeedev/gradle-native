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

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AdhocArtifactRepositoryListenerIntegrationTest {
	Project project = ProjectTestUtils.rootProject();
	@Mock
	ArtifactRepositoryGeneratorListener listener;

	@Test
	void callsListenerForComponentResolution() {
		project.getRepositories().add(AdhocArtifactRepositoryFactory.forProject(project).create("my-repo", listener));
		val configuration = project.getConfigurations().create("test", ConfigurationUtils.configureAsResolvable());
		configuration.getDependencies().add(project.getDependencies().create("com.example:foo:4.2"));

		try {
			configuration.resolve();
		} catch (RuntimeException ex) {
			// ignore
			ex.printStackTrace();
		}

		Mockito.verify(listener).onResolveComponentMetaData(any());
	}
}
