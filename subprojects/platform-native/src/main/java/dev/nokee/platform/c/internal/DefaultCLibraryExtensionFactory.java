package dev.nokee.platform.c.internal;

import dagger.MembersInjector;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.nativebase.internal.DecoratingFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponentFactory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/** @see DecoratingFactory */
public final class DefaultCLibraryExtensionFactory extends AutoDefaultCLibraryExtensionFactory {
	private final Provider<ObjectFactory> objectsProvider;
	private final Provider<ProviderFactory> providersProvider;
	private final Provider<ProjectLayout> layoutProvider;
	private final Provider<DefaultNativeLibraryComponentFactory> componentFactoryProvider;

	@Inject
	public DefaultCLibraryExtensionFactory(Provider<ObjectFactory> objectsProvider, Provider<ProviderFactory> providersProvider, Provider<ProjectLayout> layoutProvider, Provider<DefaultNativeLibraryComponentFactory> componentFactoryProvider, MembersInjector<DecoratingFactory> injector) {
		super(objectsProvider, providersProvider, layoutProvider, componentFactoryProvider);
		this.objectsProvider = objectsProvider;
		this.providersProvider = providersProvider;
		this.layoutProvider = layoutProvider;
		this.componentFactoryProvider = componentFactoryProvider;
		injector.injectMembers(this);
	}

	@Override
	public DefaultCLibraryExtension create(ComponentIdentifier identifier) {
		return newInstance(identifier, objectsProvider.get(), providersProvider.get(), layoutProvider.get(), componentFactoryProvider.get());
	}
}
