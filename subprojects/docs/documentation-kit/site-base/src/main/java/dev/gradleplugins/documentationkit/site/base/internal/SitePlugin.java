package dev.gradleplugins.documentationkit.site.base.internal;

import dev.gradleplugins.documentationkit.site.base.SiteExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;

public class SitePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SiteBasePlugin.class);
		final SiteExtension extension = project.getExtensions().create("site", SiteExtension.class);
		extension.getDestinationDirectory().fileProvider(project.getTasks().named("stageSite", Sync.class).map(Sync::getDestinationDir)).disallowChanges();
	}
}
