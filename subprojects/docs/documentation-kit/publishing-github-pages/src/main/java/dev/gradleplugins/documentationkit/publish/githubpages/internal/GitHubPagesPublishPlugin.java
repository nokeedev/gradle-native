package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import dev.gradleplugins.documentationkit.publish.githubpages.tasks.PublishToGitHubPages;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.credentials.PasswordCredentials;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.TaskProvider;

public class GitHubPagesPublishPlugin implements Plugin<Project> {
	public static final String PUBLISH_GITHUB_PAGES_LIFECYCLE_TASK_NAME = "publishToGitHubPages";

	@Override
	public void apply(Project project) {
		// TODO: Don't apply publishing plugin... it doesn't make sense as it's not a publishing element but it's own different publication
		project.getPluginManager().apply(PublishingPlugin.class);
		final TaskProvider<PublishToGitHubPages> publishTask = project.getTasks().register(PUBLISH_GITHUB_PAGES_LIFECYCLE_TASK_NAME, defaultClass(PublishToGitHubPages.class), task -> {
			task.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
			task.setDescription("Publishes site produced by this project to GitHub pages.");
			task.getCredentials().convention(project.getProviders().credentials(PasswordCredentials.class, "publishToGitHubPages"));
			task.getUri().convention(project.provider(() -> GitHubRepositoryUtils.inferGitHubHttpRepositoryUri(project.getRootDir())));
		});
		project.getTasks().named(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME, task -> task.dependsOn(publishTask));
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> defaultClass(Class<T> tClass) {
		try {
			return (Class<T>) Class.forName(tClass.getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
