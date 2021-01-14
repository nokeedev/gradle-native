package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import lombok.val;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import spock.lang.Subject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

@Subject(GitHubCredentialsFactory.class)
class GitHubCredentialsEnvironmentVariableTest {
	@Test
	@SetEnvironmentVariable(key = "GITHUB_KEY", value = "test-key")
	@SetEnvironmentVariable(key = "GITHUB_TOKEN", value = "test-token")
	void canCreateCredentialsFromGitHubKeyAndTokenEnvironmentVariables() {
		val credentials = new GitHubCredentialsFactory(providerFactory()).fromEnvironmentVariable();
		assertThat(credentials.getGitHubKey(), equalTo("test-key"));
		assertThat(credentials.getGitHubToken(), equalTo("test-token"));
	}

	@Test
	void returnsNullForMissingEnvironmentVariables() {
		val credentials = new GitHubCredentialsFactory(providerFactory()).fromEnvironmentVariable();
		assertThat(credentials.getGitHubKey(), nullValue());
		assertThat(credentials.getGitHubToken(), nullValue());
	}

	private static ProviderFactory providerFactory() {
		return ProjectBuilder.builder().build().getProviders();
	}
}
