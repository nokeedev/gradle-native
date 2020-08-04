package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.BaseIdeProjectReference;
import dev.nokee.ide.visualstudio.VisualStudioIdeGuid;
import dev.nokee.ide.visualstudio.VisualStudioIdeProjectReference;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

public class DefaultVisualStudioIdeProjectReference extends BaseIdeProjectReference implements VisualStudioIdeProjectReference {
	private final Provider<DefaultVisualStudioIdeProject> ideProject;

	public DefaultVisualStudioIdeProjectReference(Provider<DefaultVisualStudioIdeProject> ideProject) {
		super(ideProject);
		this.ideProject = ideProject;
	}

	@Override
	public Provider<FileSystemLocation> getProjectLocation() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectLocation);
	}

	public Provider<VisualStudioIdeGuid> getProjectGuid() {
		return ideProject.flatMap(VisualStudioIdeProjectReference::getProjectGuid);
	}
}
