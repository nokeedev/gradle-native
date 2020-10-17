package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetName;
import dev.nokee.language.base.internal.LanguageSourceSetRegistry;
import dev.nokee.language.swift.internal.SwiftSourceSetImpl;
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
	public DefaultSwiftIosApplicationExtension(DefaultIosApplicationComponent component, ObjectFactory objects, ProviderFactory providers, LanguageSourceSetRegistry languageSourceSetRegistry) {
		super(component, objects, providers);
		languageSourceSetRegistry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of("swift"), SwiftSourceSetImpl.class, component.getIdentifier()));
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
