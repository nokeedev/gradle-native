package dev.nokee.gradle.internal;

import dev.nokee.gradle.GradleProjectGroup;
import org.gradle.api.Project;

import javax.inject.Inject;

class GradleProjectGroupImpl implements GradleProjectGroup {
	private final Project project;

	@Inject
	public GradleProjectGroupImpl(Project project) {
		this.project = project;
	}

	@Override
	public String get() {
		return project.getGroup().toString();
	}
}
