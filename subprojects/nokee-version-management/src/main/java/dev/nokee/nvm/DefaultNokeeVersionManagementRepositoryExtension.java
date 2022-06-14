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

import groovy.lang.Closure;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;

final class DefaultNokeeVersionManagementRepositoryExtension implements NokeeVersionManagementRepositoryExtension {
	private static final Logger LOGGER = Logging.getLogger(DefaultNokeeVersionManagementRepositoryExtension.class);
	private final RepositoryHandler repositories;
	private final Provider<URI> repositoryUrlProvider;

	public DefaultNokeeVersionManagementRepositoryExtension(RepositoryHandler repositories, Provider<URI> repositoryUrlProvider) {
		this.repositories = repositories;
		this.repositoryUrlProvider = repositoryUrlProvider;

		try {
			Method target = Class.forName("dev.nokee.nvm.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
			target.invoke(null, repositories, "nokee", new NokeeClosure(repositories));
		} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			LOGGER.info("Unable to extend RepositoryHandler with nokee().");
		}
	}

	@Override
	public MavenArtifactRepository nokee() {
		return repositories.maven(new NokeeRepositoryAction(repositoryUrlProvider));
	}

	private final class NokeeClosure extends Closure<MavenArtifactRepository> {
		public NokeeClosure(RepositoryHandler handler) {
			super(handler);
		}

		public MavenArtifactRepository doCall() {
			return nokee();
		}
	}
}
