package dev.gradleplugins.documentationkit.site.githubpages;

import dev.gradleplugins.documentationkit.site.base.SiteExtension;
import org.gradle.api.provider.Property;

public interface GitHubPagesSite extends SiteExtension {
	Property<GitHubPagesCustomDomain> getCustomDomain();
	Property<String> getRepositorySlug();

	static GitHubPagesCustomDomain subdomain(String customSubdomain) {
		return GitHubPagesCustomDomain.subdomain(customSubdomain);
	}
}
