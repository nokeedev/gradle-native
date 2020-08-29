package dev.nokee.platform.base.internal.dependencies;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.gradle.internal.GradleModule;
import dev.nokee.platform.base.DependencyBucket;
import lombok.val;

import javax.inject.Provider;
import java.util.Map;

@Module(includes = {GradleModule.class})
public interface PlatformBaseDependenciesModule {
	@Provides
	@IntoMap
	@ClassKey(ResolvableDependencies.class)
	static DependencyBucketFactory<?> aResolvableDependenciesBucketFactory(DependencyBucketFactoryFactory factory, Provider<ResolvableDependencies> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(ConsumableDependencies.class)
	static DependencyBucketFactory<?> aConsumableDependenciesBucketFactory(DependencyBucketFactoryFactory factory, Provider<ConsumableDependencies> bucket) {
		return factory.wrap(bucket);
	}

	@Provides
	@IntoMap
	@ClassKey(DeclarableDependencies.class)
	static DependencyBucketFactory<?> aDeclarableDependenciesBucketFactory(DependencyBucketFactoryFactory factory, Provider<DeclarableDependencies> bucket) {
		return factory.wrap(bucket);
	}

	@Binds
	ComponentDependenciesContainerFactory aComponentDependenciesContainerFactory(ComponentDependenciesContainerFactoryImpl impl);

	/**
	 * Provides a dependency bucket instantiator preconfigured with the dagger-discovered bucket types.
	 *
	 * @param discoveredFactories dagger-discovered bucket types
	 * @return a {@link DependencyBucketInstantiator} instance, never null.
	 */
	@Provides
	static DependencyBucketInstantiator theDependencyBucketInstantiator(Map<Class<?>, DependencyBucketFactory<?>> discoveredFactories) {
		val instantiator = new DependencyBucketInstantiatorImpl();
		discoveredFactories.forEach((type, factory) -> {
			@SuppressWarnings("unchecked")
			val bucketType = (Class<DependencyBucket>) type;
			instantiator.registerFactory(bucketType, factory);
		});
		return instantiator;
	}
}
