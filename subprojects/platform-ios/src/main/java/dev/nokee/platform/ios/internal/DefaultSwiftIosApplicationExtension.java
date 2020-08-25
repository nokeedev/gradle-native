package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.LanguageSourceSetInstantiator;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.SwiftIosApplicationExtension;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

import static dev.nokee.model.DomainObjectIdentifier.named;

public class DefaultSwiftIosApplicationExtension extends BaseIosExtension<DefaultIosApplicationComponent> implements SwiftIosApplicationExtension {
	@Inject
	public DefaultSwiftIosApplicationExtension(DefaultIosApplicationComponent component, LanguageSourceSetInstantiator sourceSetInstantiator, ObjectFactory objects, ProviderFactory providers) {
		super(component, objects, providers);
		getComponent().getSourceCollection().add(sourceSetInstantiator.create(named("swift"), SwiftSourceSet.class).from("src/main/swift"));
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
