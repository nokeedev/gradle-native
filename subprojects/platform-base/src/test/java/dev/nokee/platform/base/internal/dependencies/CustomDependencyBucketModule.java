package dev.nokee.platform.base.internal.dependencies;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

import javax.inject.Provider;

@Module
public interface CustomDependencyBucketModule {
	@Provides
	@IntoMap
	@ClassKey(MyCustomDependencyBucket.class)
	static DependencyBucketFactory<?> aConfigurationFactory(DependencyBucketFactoryFactory factory, Provider<MyCustomDependencyBucket> bucketProvider) {
		return factory.wrap(bucketProvider);
	}
}
