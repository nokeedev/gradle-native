package dev.nokee.platform.ios.internal;

import dev.nokee.language.swift.internal.SwiftSourceSet;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public class DefaultSwiftIosApplicationExtension extends BaseIosExtension<DefaultIosApplicationComponent> implements SwiftIosApplicationExtension, Component {
	@Inject
	public DefaultSwiftIosApplicationExtension(DefaultIosApplicationComponent component, ObjectFactory objects, ProviderFactory providers) {
		super(component, objects, providers);
		getComponent().getSourceCollection().add(getObjects().newInstance(SwiftSourceSet.class, "swift").srcDir("src/main/swift"));
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
