package dev.nokee.platform.objectivecpp.internal;

import dagger.MembersInjector;
import dev.nokee.platform.nativebase.internal.DecoratingFactory;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/** @see DecoratingFactory */
public final class DefaultObjectiveCppApplicationExtensionFactory extends AutoDefaultObjectiveCppApplicationExtensionFactory {
	private final Provider<DefaultNativeApplicationComponent> componentProvider;
	private final Provider<ObjectFactory> objectsProvider;
	private final Provider<ProviderFactory> providersProvider;
	private final Provider<ProjectLayout> layoutProvider;

	@Inject
	public DefaultObjectiveCppApplicationExtensionFactory(Provider<DefaultNativeApplicationComponent> componentProvider, Provider<ObjectFactory> objectsProvider, Provider<ProviderFactory> providersProvider, Provider<ProjectLayout> layoutProvider, MembersInjector<DecoratingFactory> injector) {
		super(componentProvider, objectsProvider, providersProvider, layoutProvider);
		this.componentProvider = componentProvider;
		this.objectsProvider = objectsProvider;
		this.providersProvider = providersProvider;
		this.layoutProvider = layoutProvider;
		injector.injectMembers(this);
	}

	@Override
	public DefaultObjectiveCppApplicationExtension create() {
		return newInstance(componentProvider.get(), objectsProvider.get(), providersProvider.get(), layoutProvider.get());
	}
}
