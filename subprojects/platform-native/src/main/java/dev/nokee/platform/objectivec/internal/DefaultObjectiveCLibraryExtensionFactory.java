package dev.nokee.platform.objectivec.internal;

import dagger.MembersInjector;
import dev.nokee.platform.nativebase.internal.DecoratingFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/** @see DecoratingFactory */
public final class DefaultObjectiveCLibraryExtensionFactory extends AutoDefaultObjectiveCLibraryExtensionFactory {
	private final Provider<DefaultNativeLibraryComponent> componentProvider;
	private final Provider<ObjectFactory> objectsProvider;
	private final Provider<ProviderFactory> providersProvider;
	private final Provider<ProjectLayout> layoutProvider;

	@Inject
	public DefaultObjectiveCLibraryExtensionFactory(Provider<DefaultNativeLibraryComponent> componentProvider, Provider<ObjectFactory> objectsProvider, Provider<ProviderFactory> providersProvider, Provider<ProjectLayout> layoutProvider, MembersInjector<DecoratingFactory> injector) {
		super(componentProvider, objectsProvider, providersProvider, layoutProvider);
		this.componentProvider = componentProvider;
		this.objectsProvider = objectsProvider;
		this.providersProvider = providersProvider;
		this.layoutProvider = layoutProvider;
		injector.injectMembers(this);
	}

	@Override
	public DefaultObjectiveCLibraryExtension create() {
		return newInstance(componentProvider.get(), objectsProvider.get(), providersProvider.get(), layoutProvider.get());
	}
}
