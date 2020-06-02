package dev.nokee.platform.objectivec.internal;

import dev.nokee.language.base.internal.DefaultSourceSet;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.language.objectivec.internal.UTTypeObjectiveCSource;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies;
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultObjectiveCApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements ObjectiveCApplicationExtension {
	@Inject
	public DefaultObjectiveCApplicationExtension(NamingScheme names) {
		super(names, DefaultNativeApplicationComponent.class);
		getComponent().getSourceCollection().add(getObjects().newInstance(ObjectiveCSourceSet.class).srcDir("src/main/objc"));
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
