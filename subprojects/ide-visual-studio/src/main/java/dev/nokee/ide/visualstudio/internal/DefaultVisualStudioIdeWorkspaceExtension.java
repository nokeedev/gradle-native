package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.VisualStudioIdeWorkspaceExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class DefaultVisualStudioIdeWorkspaceExtension extends DefaultVisualStudioIdeProjectExtension implements VisualStudioIdeWorkspaceExtension, IdeWorkspaceExtension<VisualStudioIdeProject>, HasPublicType {
	private final DefaultVisualStudioIdeSolution solution;

	@Inject
	public DefaultVisualStudioIdeWorkspaceExtension() {
		this.solution = getObjects().newInstance(DefaultVisualStudioIdeSolution.class);
	}

	@Override
	public DefaultVisualStudioIdeSolution getSolution() {
		return solution;
	}

	@Override
	public DefaultVisualStudioIdeSolution getWorkspace() {
		return solution;
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(VisualStudioIdeWorkspaceExtension.class);
	}
}
