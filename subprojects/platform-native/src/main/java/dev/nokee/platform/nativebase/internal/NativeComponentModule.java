package dev.nokee.platform.nativebase.internal;

import dagger.Module;
import dagger.Provides;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.base.internal.NamingSchemeFactory;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

@Module
public interface NativeComponentModule {
	@Provides
	static NamingSchemeFactory aNamingSchemeFactory(Project project) {
		return new NamingSchemeFactory(project.getName());
	}

	@Provides
	static NamingScheme aNamingScheme(NamingSchemeFactory factory) {
		return factory.forMainComponent().withComponentDisplayName("main native component");
	}

	@Provides
	static ComponentDependenciesInternal aComponentDependencies(NamingScheme names, ConfigurationContainer configurations, DependencyHandler dependencyHandler, ObjectFactory objectFactory) {
		return objectFactory.newInstance(DefaultComponentDependencies.class, names.getComponentDisplayName(), new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(configurations), names::getConfigurationName), new DefaultDependencyFactory(dependencyHandler))));
	}

	@Provides
	static DefaultNativeApplicationComponentDependencies aApplicationComponentDependencies(ComponentDependenciesInternal dependencies, ObjectFactory objectFactory) {
		return objectFactory.newInstance(DefaultNativeApplicationComponentDependencies.class, dependencies);
	}

	@Provides
	static DefaultNativeLibraryComponentDependencies aLibraryComponentDependencies(ComponentDependenciesInternal dependencies, ObjectFactory objectFactory) {
		return objectFactory.newInstance(DefaultNativeLibraryComponentDependencies.class, dependencies);
	}
}
