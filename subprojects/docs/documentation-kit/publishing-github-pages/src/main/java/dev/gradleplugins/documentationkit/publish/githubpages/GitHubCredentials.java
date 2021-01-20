package dev.gradleplugins.documentationkit.publish.githubpages;

import lombok.EqualsAndHashCode;
import org.gradle.api.tasks.Input;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;

@EqualsAndHashCode
public final class GitHubCredentials {
	private final Supplier<String> keySupplier;
	private final Supplier<String> tokenSupplier;

	public GitHubCredentials(Supplier<String> keySupplier, Supplier<String> tokenSupplier) {
		this.keySupplier = keySupplier;
		this.tokenSupplier = tokenSupplier;
	}

	@Input
	public String getGitHubKey() {
		return keySupplier.get();
	}

	@Input
	public String getGitHubToken() {
		return tokenSupplier.get();
	}

	public static GitHubCredentials of(String key, String token) {
		return new GitHubCredentials(ofInstance(key), ofInstance(token));
	}
}
