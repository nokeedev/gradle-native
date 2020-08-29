package dev.nokee.platform.nativebase.internal.dependencies;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryFactory;
import dev.nokee.platform.base.internal.dependencies.PlatformBaseDependenciesModule;

import javax.inject.Provider;

@Module(includes = {PlatformBaseDependenciesModule.class})
public interface PlatformNativeDependenciesModule {
	@Provides
	@IntoMap
	@ClassKey(ConsumableLinkLibraries.class)
	static DependencyBucketFactory<?> theConsumableLinkLibrariesFactory(DependencyBucketFactoryFactory factory, Provider<ConsumableLinkLibraries> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ConsumableRuntimeLibraries.class)
	static DependencyBucketFactory<?> theConsumableRuntimeLibrariesFactory(DependencyBucketFactoryFactory factory, Provider<ConsumableRuntimeLibraries> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ConsumableNativeHeaders.class)
	static DependencyBucketFactory<?> theConsumableNativeHeadersFactory(DependencyBucketFactoryFactory factory, Provider<ConsumableNativeHeaders> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ConsumableSwiftModules.class)
	static DependencyBucketFactory<?> theConsumableSwiftModulesFactory(DependencyBucketFactoryFactory factory, Provider<ConsumableSwiftModules> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ResolvableLinkLibraries.class)
	static DependencyBucketFactory<?> theResolvableLinkLibrariesFactory(DependencyBucketFactoryFactory factory, Provider<ResolvableLinkLibraries> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ResolvableNativeHeaders.class)
	static DependencyBucketFactory<?> theResolvableNativeHeadersFactory(DependencyBucketFactoryFactory factory, Provider<ResolvableNativeHeaders> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ResolvableSwiftModules.class)
	static DependencyBucketFactory<?> theResolvableSwiftModulesFactory(DependencyBucketFactoryFactory factory, Provider<ResolvableSwiftModules> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ResolvableRuntimeLibraries.class)
	static DependencyBucketFactory<?> theResolvableRuntimeLibrariesFactory(DependencyBucketFactoryFactory factory, Provider<ResolvableRuntimeLibraries> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(DeclarableMacOsFrameworkAwareDependencies.class)
	static DependencyBucketFactory<?> theFrameworkAwareDeclaringDependenciesFactory(DependencyBucketFactoryFactory factory, Provider<DeclarableMacOsFrameworkAwareDependencies> bucket) {
		return factory.wrap(bucket);
	}
}
