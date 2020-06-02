package dev.nokee.platform.ios.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.internal.GroupId;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultSwiftIosApplicationExtension extends BaseIosExtension<DefaultIosApplicationComponent> implements SwiftIosApplicationExtension {
	@Inject
	public DefaultSwiftIosApplicationExtension(NamingScheme names, GroupId groupId) {
		super(names, groupId, DefaultIosApplicationComponent.class);
		getComponent().getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class).srcDir("src/main/swift"));
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeComponentDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}
}
