package dev.nokee.platform.ios.internal;

import dev.nokee.language.objectivec.internal.ObjectiveCSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.Project;

import javax.inject.Inject;

public abstract class DefaultObjectiveCIosApplicationExtension extends BaseIosExtension<DefaultIosApplicationComponent> implements ObjectiveCIosApplicationExtension {
	@Inject
	public DefaultObjectiveCIosApplicationExtension(DefaultIosApplicationComponent component) {
		super(component);
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

	@Override
	public VariantView<IosApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(IosApplication.class);
	}
}
