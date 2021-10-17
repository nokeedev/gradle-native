package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import com.google.common.collect.ImmutableMap;
import dev.gradleplugins.documentationkit.publish.githubpages.tasks.PublishToGitHubPages;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

class GitHubPagesPublishPluginIntegrationTest {
	private final Project project = ProjectBuilder.builder().build();

	private Project applyPluginUnderTest() {
		project.apply(ImmutableMap.of("plugin", "dev.gradleplugins.documentation.github-pages-publish"));
		return project;
	}

	@Test
	void createPublishTask() {
		val publishToGitHubPages = applyPluginUnderTest().getTasks().withType(PublishToGitHubPages.class).findByName("publishToGitHubPages");
		assertThat(publishToGitHubPages.getGroup(), equalTo("publishing"));
		assertThat(publishToGitHubPages.getDescription(), equalTo("Publishes site produced by this project to GitHub pages."));
		assertThat(publishToGitHubPages.getCredentials().isPresent(), equalTo(true));
	}

	@Test
	void appliesPublishingPlugin() {
		assertThat(applyPluginUnderTest().getPlugins().hasPlugin(PublishingPlugin.class), equalTo(true));
	}

	@Test
	void addsDependencyFromPublishLifecycleTaskToGitHubPagesPublishTask() {
		assertThat(applyPluginUnderTest().getTasks().getByName("publish"), dependsOn(":publishToGitHubPages"));
	}

	private static Matcher<Task> dependsOn(String taskPath) {
		return new FeatureMatcher<Task, Set<String>>(hasItem(taskPath), "", "") {
			@Override
			protected Set<String> featureValueOf(Task actual) {
				return actual.getDependsOn().stream().map(it -> {
					if (it instanceof TaskProvider) {
						// TODO: Account for non-root project
						return actual.getProject().getPath() + ((TaskProvider<?>) it).getName();
					} else if (it instanceof String) {
						// TODO: Account for non-root project
						return actual.getProject().getPath() + it;
					} else if (it instanceof Task) {
						return ((Task) it).getPath();
					}
					// TODO: missing list of all the same
					// TODO: Callable
					// TODO: Provider
					throw new UnsupportedOperationException();
				}).collect(Collectors.toSet());
			}
		};
	}
}
