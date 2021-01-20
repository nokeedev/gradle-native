package dev.gradleplugins.documentationkit.site.githubpages;

import org.gradle.api.provider.Property;

public interface GitHubPagesSite {
	Property<GitHubPagesCustomDomain> getCustomDomain();
	Property<String> getRepositorySlug();

	static GitHubPagesCustomDomain subdomain(String customSubdomain) {
		return GitHubPagesCustomDomain.subdomain(customSubdomain);
	}
}
