package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.xcode.XcodeIdeWorkspaceExtension;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeWorkspaceExtension extends DefaultXcodeIdeProjectExtension implements XcodeIdeWorkspaceExtension, IdeWorkspaceExtension {
	private final DefaultXcodeIdeWorkspace workspace;

	@Inject
	public DefaultXcodeIdeWorkspaceExtension() {
		this.workspace = getObjects().newInstance(DefaultXcodeIdeWorkspace.class);
	}

	@Override
	public DefaultXcodeIdeWorkspace getWorkspace() {
		return workspace;
	}
}
