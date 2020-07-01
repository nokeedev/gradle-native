package dev.nokee.ide.base.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public abstract class AbstractIdePlugin implements Plugin<Project> {
	public static final String IDE_GROUP_NAME = "IDE";

	@Override
	public final void apply(Project project) {
		doApply(project);
	}

	protected abstract void doApply(Project project);
}
