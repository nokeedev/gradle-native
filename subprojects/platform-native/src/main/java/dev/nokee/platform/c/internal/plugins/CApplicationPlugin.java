package dev.nokee.platform.c.internal.plugins;

import dev.nokee.platform.c.CApplicationExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class CApplicationPlugin implements Plugin<Project> {
	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void apply(Project project) {
		project.getExtensions().add(CApplicationExtension.class, "application", getObjects().newInstance(CApplicationExtension.class));
	}
}
