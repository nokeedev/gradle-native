package dev.gradleplugins.documentationkit.site.githubpages.internal;

import dev.gradleplugins.documentationkit.publish.githubpages.tasks.PublishToGitHubPages;
import dev.gradleplugins.documentationkit.site.githubpages.GitHubPagesSite;
import dev.gradleplugins.documentationkit.site.githubpages.tasks.GenerateGitHubPagesCustomDomainCanonicalNameRecord;
import dev.gradleplugins.documentationkit.site.githubpages.tasks.GenerateGitHubPagesNoJekyll;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;

import static dev.gradleplugins.documentationkit.publish.githubpages.internal.GitHubPagesPublishPlugin.PUBLISH_GITHUB_PAGES_LIFECYCLE_TASK_NAME;

public class GitHubPagesSitePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val site = project.getExtensions().create("site", GitHubPagesSite.class);

		val customDomainTask = project.getTasks().register("generateCustomDomainAlias", GenerateGitHubPagesCustomDomainCanonicalNameRecord.class, task -> {
			task.getOutputFile().value(project.getLayout().getBuildDirectory().file("CNAME")).disallowChanges();
			task.getCustomDomain().convention(site.getCustomDomain());
		});

		val noJekyllTask = project.getTasks().register("generateNoJekyll", GenerateGitHubPagesNoJekyll.class);

		val stageSiteTask = project.getTasks().register("stageSite", Sync.class, task -> {
			task.from(customDomainTask.map(GenerateGitHubPagesCustomDomainCanonicalNameRecord::getOutputFile));
			task.from(noJekyllTask.map(GenerateGitHubPagesNoJekyll::getOutputFile));
			task.setDestinationDir(project.getLayout().getBuildDirectory().dir("site").get().getAsFile());
		});

		project.getTasks().register("site", task -> {
			task.dependsOn(stageSiteTask);
		});

		project.getPluginManager().apply("dev.gradleplugins.documentation.github-pages-publish");

		project.getTasks().named(PUBLISH_GITHUB_PAGES_LIFECYCLE_TASK_NAME, PublishToGitHubPages.class, task -> {
			task.getUri().convention(site.getRepositorySlug().map(it -> project.uri("https://github.com/" + it + ".git")));
			task.getPublishDirectory().fileProvider(stageSiteTask.map(Sync::getDestinationDir));
		});
	}
}
