package dev.nokee.platform.jni.internal;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryFactory;
import dev.nokee.platform.nativebase.internal.dependencies.PlatformNativeDependenciesModule;

import javax.inject.Provider;

@Module(includes = {PlatformNativeDependenciesModule.class})
public interface PlatformJniDependenciesModule {
	@Provides
	@IntoMap
	@ClassKey(ConsumableJvmApiElements.class)
	static DependencyBucketFactory<?> theConsumableJvmApiElements(DependencyBucketFactoryFactory factory, Provider<ConsumableJvmApiElements> bucketProvider) {
		return factory.wrap(bucketProvider);
	}

	@Provides
	@IntoMap
	@ClassKey(ConsumableJvmRuntimeElements.class)
	static DependencyBucketFactory<?> theConsumableJvmRuntimeElements(DependencyBucketFactoryFactory factory, Provider<ConsumableJvmRuntimeElements> bucketProvider) {
		return factory.wrap(bucketProvider);
	}
}
