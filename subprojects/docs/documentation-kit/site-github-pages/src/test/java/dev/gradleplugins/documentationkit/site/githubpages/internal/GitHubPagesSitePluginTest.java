package dev.gradleplugins.documentationkit.site.githubpages.internal;

import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.documentationkit.publish.githubpages.tasks.PublishToGitHubPages;
import dev.gradleplugins.documentationkit.site.githubpages.GitHubPagesSite;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class GitHubPagesSitePluginTest {
	@Test
	void applyGitHubPagesPublishPlugin() {
		assertThat(applyPluginUnderTest().getPluginManager().hasPlugin("dev.gradleplugins.documentation.github-pages-publish"), equalTo(true));
	}

	@Test
	void configuresGitHubPagesPublishUriWithRepositorySlug() {
		val project = applyPluginUnderTest();
		project.getExtensions().getByType(GitHubPagesSite.class).getRepositorySlug().set("foo/bar");
		assertThat(project.getTasks().withType(PublishToGitHubPages.class).getByName("publishToGitHubPages").getUri().get(),
			hasToString("https://github.com/foo/bar.git"));
	}

	private static Project applyPluginUnderTest() {
		val project = ProjectBuilder.builder().build();
		project.apply(ImmutableMap.of("plugin", "dev.gradleplugins.documentation.github-pages-site"));
		return project;
	}
}
