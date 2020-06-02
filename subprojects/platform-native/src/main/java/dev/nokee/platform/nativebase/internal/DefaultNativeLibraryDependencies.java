package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryDependencies extends AbstractNativeComponentDependencies implements NativeLibraryDependencies {
	private final DependencyBucket api;

	@Inject
	public DefaultNativeLibraryDependencies(NamingScheme names) {
		super(names);
		this.api = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create(names.getConfigurationName("api"), ConfigurationUtils::configureAsBucket));

		getImplementationDependencies().extendsFrom(getApiDependencies());
	}

	@Override
	public void api(Object notation) {
		api.addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		api.addDependency(notation, action);
	}

	public Configuration getApiDependencies() {
		return api.getAsConfiguration();
	}

	public DefaultNativeLibraryDependencies extendsFrom(DefaultNativeLibraryDependencies dependencies) {
		getApiDependencies().extendsFrom(dependencies.getApiDependencies());
		getImplementationDependencies().extendsFrom(dependencies.getImplementationDependencies());
		getCompileOnlyDependencies().extendsFrom(dependencies.getCompileOnlyDependencies());
		getLinkOnlyDependencies().extendsFrom(dependencies.getLinkOnlyDependencies());
		getRuntimeOnlyDependencies().extendsFrom(dependencies.getRuntimeOnlyDependencies());
		return this;
	}
}
