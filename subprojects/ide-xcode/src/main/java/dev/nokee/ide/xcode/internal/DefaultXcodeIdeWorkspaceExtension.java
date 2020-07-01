package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeWorkspaceExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class DefaultXcodeIdeWorkspaceExtension extends DefaultXcodeIdeProjectExtension implements XcodeIdeWorkspaceExtension, IdeWorkspaceExtension<XcodeIdeProject>, HasPublicType {
	private final DefaultXcodeIdeWorkspace workspace;

	@Inject
	public DefaultXcodeIdeWorkspaceExtension() {
		this.workspace = getObjects().newInstance(DefaultXcodeIdeWorkspace.class);
	}

	@Override
	public DefaultXcodeIdeWorkspace getWorkspace() {
		return workspace;
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(XcodeIdeWorkspaceExtension.class);
	}
}
