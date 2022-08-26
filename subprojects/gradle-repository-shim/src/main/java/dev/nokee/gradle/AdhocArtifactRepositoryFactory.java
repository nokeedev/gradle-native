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
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.Directory;
import org.gradle.api.internal.artifacts.BaseRepositoryFactory;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class AdhocArtifactRepositoryFactory {
	private final MavenArtifactRepositoryFactory mavenRepositoryFactory;
	private final ArtifactRepositoryFactory factory;
	private final ArtifactRepositoryPathProvider repositoryPathProvider;

	public AdhocArtifactRepositoryFactory(Project project) {
		this.mavenRepositoryFactory = ((ProjectInternal) project).getServices().get(BaseRepositoryFactory.class)::createMavenRepository;
		this.repositoryPathProvider = name -> project.getLayout().getBuildDirectory().dir("adhoc-m2/" + name);
		if (GradleVersion.current().compareTo(GradleVersion.version("6.6")) >= 0) {
			this.factory = new ArtifactRepositoryFactory("dev.nokee.gradle.internal.repositories.v66.DefaultAdhocArtifactRepository");
		} else {
			throw new UnsupportedOperationException("Gradle version lower than 6.5 are not supported. " + GradleVersion.current());
		}
	}

	public static AdhocArtifactRepositoryFactory forProject(Project project) {
		return new AdhocArtifactRepositoryFactory(project);
	}

	// TODO: Remove the parameters, the responsability is a bit too much here.
	public ArtifactRepository create(String name, ArtifactRepositoryGeneratorListener listener) {
		val delegate = mavenRepositoryFactory.create();

		// TODO: Remove this responsability
		delegate.setName(name);
		delegate.setUrl(repositoryPathProvider.forName(name));

		// Force only Gradle Metadata as it's the richest representation
		delegate.metadataSources(MavenArtifactRepository.MetadataSources::gradleMetadata);
		return factory.create(delegate, listener);
	}

	private static final class ArtifactRepositoryFactory {
		private final Constructor<?> constructor;

		private ArtifactRepositoryFactory(String className) {
			try {
				this.constructor = Class.forName(className).getConstructor(MavenArtifactRepository.class, ArtifactRepositoryGeneratorListener.class);
			} catch (NoSuchMethodException | ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		@SuppressWarnings("unchecked")
		public ArtifactRepository create(MavenArtifactRepository delegate, ArtifactRepositoryGeneratorListener listener) {
			try {
				return (ArtifactRepository) constructor.newInstance(delegate, listener);
			} catch (
				InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private interface MavenArtifactRepositoryFactory {
		MavenArtifactRepository create();
	}

	private interface ArtifactRepositoryPathProvider {
		Provider<Directory> forName(String name);
	}
}
