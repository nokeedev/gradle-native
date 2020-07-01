package dev.nokee.ide.base.internal;

import dev.nokee.ide.base.IdeWorkspace;

public interface IdeWorkspaceExtension extends IdeProjectExtension {
	IdeWorkspace getWorkspace();
}
