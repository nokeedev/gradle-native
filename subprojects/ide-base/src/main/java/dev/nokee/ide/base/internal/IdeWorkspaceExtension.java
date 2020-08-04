package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.IdeProjectReference;

public interface IdeWorkspaceExtension<T extends IdeProject> extends IdeProjectExtension<T> {
	IdeWorkspaceInternal<? extends IdeProjectReference> getWorkspace();
}
