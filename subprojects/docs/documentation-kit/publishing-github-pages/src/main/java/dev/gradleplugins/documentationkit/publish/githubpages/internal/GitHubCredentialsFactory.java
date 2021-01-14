package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import dev.gradleplugins.documentationkit.publish.githubpages.GitHubCredentials;
import org.gradle.api.provider.ProviderFactory;

public final class GitHubCredentialsFactory {
	private final ProviderFactory providerFactory;

	public GitHubCredentialsFactory(ProviderFactory providerFactory) {
		this.providerFactory = providerFactory;
	}

	public GitHubCredentials fromEnvironmentVariable() {
		return new GitHubCredentials(providerFactory.environmentVariable("GITHUB_KEY")::getOrNull, providerFactory.environmentVariable("GITHUB_TOKEN")::getOrNull);
	}
}
