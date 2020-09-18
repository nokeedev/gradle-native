package dev.nokee.platform.base.internal.plugins;

import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.internal.ProjectIdentifier;
import dev.nokee.platform.base.internal.components.ComponentContainerImpl;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ComponentBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("lifecycle-base");

		val extension = new ComponentContainerImpl(ProjectIdentifier.of(project), project.getObjects());
		project.getExtensions().add(ComponentContainer.class, "components", extension);
		project.afterEvaluate(proj -> extension.disallowChanges());
	}
}
