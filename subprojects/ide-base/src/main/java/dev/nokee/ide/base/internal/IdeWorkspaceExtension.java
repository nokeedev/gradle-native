package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;

public interface IdeWorkspaceExtension<T extends IdeProject> extends IdeProjectExtension<T> {
	IdeWorkspaceInternal<T> getWorkspace();
}
