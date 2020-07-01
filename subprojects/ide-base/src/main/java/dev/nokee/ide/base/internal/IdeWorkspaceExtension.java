package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeProject;
import dev.nokee.ide.base.IdeWorkspace;

public interface IdeWorkspaceExtension<T extends IdeProject> extends IdeProjectExtension<T> {
	IdeWorkspace<T> getWorkspace();
}
