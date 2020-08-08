package dev.nokee.platform.swift.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.swift.SwiftApplicationExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultSwiftApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements SwiftApplicationExtension {
	@Inject
	public DefaultSwiftApplicationExtension(DefaultNativeApplicationComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class, "swift").from(getSources().getElements().map(toIfEmpty("src/main/swift"))));
	}

	@Override
	public NativeApplicationComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	@Override
	public void dependencies(Action<? super NativeApplicationComponentDependencies> action) {
		getComponent().dependencies(action);
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<NativeApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeApplication.class);
	}
}
