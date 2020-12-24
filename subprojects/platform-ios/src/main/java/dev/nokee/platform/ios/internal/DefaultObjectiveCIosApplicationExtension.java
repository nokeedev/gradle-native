package dev.nokee.platform.ios.internal;

import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.HasLanguageSourceSetAccessor;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

public class DefaultObjectiveCIosApplicationExtension extends BaseIosExtension<DefaultIosApplicationComponent> implements ObjectiveCIosApplicationExtension, Component, HasLanguageSourceSetAccessor {
	public DefaultObjectiveCIosApplicationExtension(DefaultIosApplicationComponent component, ObjectFactory objects, ProviderFactory providers) {
		super(component, objects, providers);
	}

	@Override
	public NativeComponentDependencies getDependencies() {
		return getComponent().getDependencies();
	}

	public void finalizeExtension(Project project) {
		getComponent().finalizeExtension(project);
	}

	@Override
	public VariantView<IosApplication> getVariants() {
		return getComponent().getVariantCollection().getAsView(IosApplication.class);
	}
}
