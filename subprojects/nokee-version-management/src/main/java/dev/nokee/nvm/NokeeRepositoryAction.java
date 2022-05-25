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
package dev.nokee.nvm;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.provider.Provider;

import java.net.URI;

final class NokeeRepositoryAction implements Action<RepositoryHandler> {
	private final Provider<URI> repositoryUrl;

	public NokeeRepositoryAction(Provider<URI> repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	@Override
	public void execute(RepositoryHandler repositories) {
		repositories.maven(this::nokeeRepository);
	}

	private void nokeeRepository(MavenArtifactRepository repository) {
		repository.setName("Nokee Artifact Repository");
		repository.setUrl(repositoryUrl);
		repository.mavenContent(content -> {
			content.includeGroupByRegex("dev\\.nokee.*");
		});
	}
}
