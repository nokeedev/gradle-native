package dev.nokee.platform.objectivecpp.internal;

import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeExtension;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultObjectiveCppApplicationExtension extends BaseNativeExtension<DefaultNativeApplicationComponent> implements ObjectiveCppApplicationExtension {
	@Inject
	public DefaultObjectiveCppApplicationExtension(DefaultNativeApplicationComponent component) {
		super(component);
		getComponent().getSourceCollection().add(getObjects().newInstance(ObjectiveCppSourceSet.class).srcDir("src/main/objcpp"));
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

	@Override
	public VariantView<NativeApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(NativeApplication.class);
	}
}
