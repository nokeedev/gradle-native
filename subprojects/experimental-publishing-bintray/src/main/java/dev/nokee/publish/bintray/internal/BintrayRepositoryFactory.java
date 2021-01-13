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
