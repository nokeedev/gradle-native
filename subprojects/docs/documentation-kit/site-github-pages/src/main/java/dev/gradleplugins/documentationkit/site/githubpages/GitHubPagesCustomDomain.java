package dev.gradleplugins.documentationkit.site.githubpages;

import java.io.Serializable;

public final class GitHubPagesCustomDomain implements Serializable {
	private final String customDomain;

	private GitHubPagesCustomDomain(String customDomain) {
		this.customDomain = customDomain;
	}

	public String get() {
		return customDomain;
	}

	public static GitHubPagesCustomDomain subdomain(String customSubdomain) {
		// TODO: Assert valid custom subdomain.
		return new GitHubPagesCustomDomain(customSubdomain);
	}
}
