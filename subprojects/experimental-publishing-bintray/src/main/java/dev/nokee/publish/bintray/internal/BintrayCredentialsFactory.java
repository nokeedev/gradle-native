package dev.nokee.publish.bintray.internal;

import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.function.Supplier;

public final class BintrayCredentialsFactory {
	private final ProviderFactory providerFactory;

	@Inject
	public BintrayCredentialsFactory(ProviderFactory providerFactory) {
		this.providerFactory = providerFactory;
	}

	public BintrayCredentials fromEnvironmentVariable(String user, String key, BintrayCredentials parent) {
		return BintrayCredentials.builder()
			.withUser(providerFactory.environmentVariable(user)::getOrNull)
			.withKey(providerFactory.environmentVariable(key)::getOrNull)
			.withParent(parent)
			.build();
	}

	public BintrayCredentials fromGradleProperty(Supplier<String> user, Supplier<String> key, BintrayCredentials parent) {
		return BintrayCredentials.builder()
			.withUser(providerFactory.gradleProperty(providerFactory.provider(user::get))::getOrNull)
			.withKey(providerFactory.gradleProperty(providerFactory.provider(key::get))::getOrNull)
			.withParent(parent)
			.build();
	}

	public BintrayCredentials fromSystemProperty(Supplier<String> user, Supplier<String> key, BintrayCredentials parent) {
		return BintrayCredentials.builder()
			.withUser(providerFactory.systemProperty(providerFactory.provider(user::get))::getOrNull)
			.withKey(providerFactory.systemProperty(providerFactory.provider(key::get))::getOrNull)
			.withParent(parent)
			.build();
	}

	public BintrayCredentials fromRepositoryDeclaration(MavenArtifactRepository repository, BintrayCredentials parent) {
		return BintrayCredentials.builder().withUser(repository.getCredentials()::getUsername).withKey(repository.getCredentials()::getPassword).withParent(parent).build();
	}

//	public BintrayCredentials defaultCredentials(MavenArtifactRepository repository, GradleProjectGroup group) {
//		return fromRepositoryDeclaration(repository, defaultGradlePropertyCredentials(group, defaultSystemPropertyCredentials(group, defaultEnvironmentVariableCredentials(invalidBintrayCredentials()))));
//	}
//
//	private BintrayCredentials defaultEnvironmentVariableCredentials(BintrayCredentials parent) {
//		return fromEnvironmentVariable("BINTRAY_USER", "BINTRAY_KEY", parent);
//	}
//
//	private BintrayCredentials defaultSystemPropertyCredentials(GradleProjectGroup group, BintrayCredentials parent) {
//		return fromSystemProperty(providerFactory.provider(() -> group.get() + ".env.BINTRAY_USER"), providerFactory.provider(() -> group.get() + ".env.BINTRAY_KEY"), parent);
//	}
//
//	private BintrayCredentials defaultGradlePropertyCredentials(GradleProjectGroup group, BintrayCredentials parent) {
//		return fromGradleProperty(providerFactory.provider(() -> group.get() + ".env.BINTRAY_USER"), providerFactory.provider(() -> group.get() + ".env.BINTRAY_KEY"), parent);
//	}
}
