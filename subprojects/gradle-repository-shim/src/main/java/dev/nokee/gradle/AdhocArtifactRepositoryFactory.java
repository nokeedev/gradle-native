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

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class AdhocArtifactRepositoryFactory {
	private static final String ADHOC_REPO_DEFAULT_NAME = "adhoc";

	private final MavenArtifactRepositoryFactory mavenRepositoryFactory;
	private final ArtifactRepositoryFactory factory;
	private final ObjectFactory objects;
	private final ProviderFactory providers;

	public AdhocArtifactRepositoryFactory(Project project) {
		this.objects = project.getObjects();
		this.providers = project.getProviders();
		this.mavenRepositoryFactory = ((ProjectInternal) project).getServices().get(BaseRepositoryFactory.class)::createMavenRepository;
		if (GradleVersion.current().compareTo(GradleVersion.version("6.6")) >= 0) {
			this.factory = new ArtifactRepositoryFactory("dev.nokee.gradle.internal.repositories.v66.DefaultAdhocArtifactRepository");
		} else {
			throw new UnsupportedOperationException("Gradle version lower than 6.5 are not supported. " + GradleVersion.current());
		}
	}

	public static AdhocArtifactRepositoryFactory forProject(Project project) {
		return new AdhocArtifactRepositoryFactory(project);
	}

	public AdhocArtifactRepository create() {
		val delegate = mavenRepositoryFactory.create();

		delegate.setName(ADHOC_REPO_DEFAULT_NAME);

		// Force only Gradle Metadata as it's the richest representation
		delegate.metadataSources(MavenArtifactRepository.MetadataSources::gradleMetadata);
		return factory.create(delegate, objects);
	}

	private static final class ArtifactRepositoryFactory {
		private final Constructor<?> constructor;

		private ArtifactRepositoryFactory(String className) {
			try {
				this.constructor = Class.forName(className).getConstructor(MavenArtifactRepository.class, ObjectFactory.class);
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("unchecked")
		public AdhocArtifactRepository create(MavenArtifactRepository delegate, ObjectFactory objects) {
			try {
				return (AdhocArtifactRepository) constructor.newInstance(delegate, objects);
			} catch (
				InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private interface MavenArtifactRepositoryFactory {
		MavenArtifactRepository create();
	}
}
