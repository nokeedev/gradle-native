package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.BaseIdeProjectMetadata;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import org.gradle.api.provider.Provider;

public class VisualStudioIdeProjectMetadata extends BaseIdeProjectMetadata {
	private final Provider<DefaultVisualStudioIdeProject> ideProject;

	public VisualStudioIdeProjectMetadata(Provider<? extends IdeProjectInternal> ideProject) {
		super(ideProject);
		this.ideProject = ideProject.map(DefaultVisualStudioIdeProject.class::cast);
	}

	public VisualStudioIdeGuid getProjectGuid() {
		return ideProject.get().getProjectGuid().get();
	}
}
