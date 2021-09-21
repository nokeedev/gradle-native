/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.publish.bintray.internal;

import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import javax.inject.Inject;

import static dev.nokee.publish.bintray.internal.BintrayCredentials.invalidBintrayCredentials;
import static dev.nokee.publish.bintray.internal.BintrayPackageName.fromRepositoryDeclaration;

public final class BintrayRepositoryFactory {
	private final BintrayCredentialsFactory credentialsFactory;
	private final GradleProjectGroup group;

	@Inject
	public BintrayRepositoryFactory(BintrayCredentialsFactory credentialsFactory, GradleProjectGroup group) {
		this.credentialsFactory = credentialsFactory;
		this.group = group;
	}

	public BintrayRepository create(MavenArtifactRepository repository) {
		return new BintrayRepository(repository.getUrl(), fromRepositoryDeclaration(repository), defaultCredentials(repository));
	}

	public BintrayCredentials defaultCredentials(MavenArtifactRepository repository) {
		return credentialsFactory.fromRepositoryDeclaration(repository, defaultGradlePropertyCredentials(group, defaultSystemPropertyCredentials(group, defaultEnvironmentVariableCredentials(invalidBintrayCredentials()))));
	}

	private BintrayCredentials defaultEnvironmentVariableCredentials(BintrayCredentials parent) {
		return credentialsFactory.fromEnvironmentVariable("BINTRAY_USER", "BINTRAY_KEY", parent);
	}

	private BintrayCredentials defaultSystemPropertyCredentials(GradleProjectGroup group, BintrayCredentials parent) {
		return credentialsFactory.fromSystemProperty(() -> group.get() + ".env.BINTRAY_USER", () -> group.get() + ".env.BINTRAY_KEY", parent);
	}

	private BintrayCredentials defaultGradlePropertyCredentials(GradleProjectGroup group, BintrayCredentials parent) {
		return credentialsFactory.fromGradleProperty(() -> group.get() + ".env.BINTRAY_USER", () -> group.get() + ".env.BINTRAY_KEY", parent);
	}
}
