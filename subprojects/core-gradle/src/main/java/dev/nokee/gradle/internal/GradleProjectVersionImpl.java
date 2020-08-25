package dev.nokee.gradle.internal;

import dev.nokee.gradle.GradleProjectVersion;
import org.gradle.api.Project;

import javax.inject.Inject;

class GradleProjectVersionImpl implements GradleProjectVersion {
	private final Project project;

	@Inject
	public GradleProjectVersionImpl(Project project) {
		this.project = project;
	}

	@Override
	public String get() {
		return project.getVersion().toString();
	}
}
