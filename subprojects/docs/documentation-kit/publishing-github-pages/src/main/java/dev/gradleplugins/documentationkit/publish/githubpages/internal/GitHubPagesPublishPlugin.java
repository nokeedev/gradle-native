package dev.gradleplugins.documentationkit.publish.githubpages.internal;

import dev.gradleplugins.documentationkit.publish.githubpages.tasks.PublishToGitHubPages;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.publish.plugins.PublishingPlugin;

public class GitHubPagesPublishPlugin implements Plugin<Project> {
	public static final String PUBLISH_GITHUB_PAGES_LIFECYCLE_TASK_NAME = "publishToGitHubPages";

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(PublishingPlugin.class);
		val credentialsFactory = new GitHubCredentialsFactory(project.getProviders());
		val publishTask = project.getTasks().register(PUBLISH_GITHUB_PAGES_LIFECYCLE_TASK_NAME, defaultClass(PublishToGitHubPages.class), task -> {
			task.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
			task.setDescription("Publishes site produced by this project to GitHub pages.");
			task.getCredentials().convention(credentialsFactory.fromEnvironmentVariable());
			task.getUri().convention(project.provider(() -> GitHubRepositoryUtils.inferGitHubHttpRepositoryUri(project.getRootDir())));
		});
		project.getTasks().named(PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME, task -> task.dependsOn(publishTask));
	}

	@SneakyThrows
	@SuppressWarnings("unchecked")
	private static <T> Class<T> defaultClass(Class<T> tClass) {
		return (Class<T>) Class.forName(tClass.getName());
	}
}
