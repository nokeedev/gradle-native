package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.BaseIdeProjectReference;
import dev.nokee.ide.visualstudio.VisualStudioIdeGuid;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectReference;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.util.Set;

public final class DefaultVisualStudioIdeProjectReference extends BaseIdeProjectReference implements VisualStudioIdeProjectReference {
	private final Provider<DefaultVisualStudioIdeProject> ideProject;

	public DefaultVisualStudioIdeProjectReference(Provider<DefaultVisualStudioIdeProject> ideProject) {
		super(ideProject);
		this.ideProject = ideProject;
	}

	@Override
	public Provider<FileSystemLocation> getProjectLocation() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectLocation);
	}

	@Override
	public Provider<VisualStudioIdeGuid> getProjectGuid() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectGuid);
	}

	@Override
	public Provider<Set<VisualStudioIdeProjectConfiguration>> getProjectConfigurations() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectConfigurations);
	}
}
