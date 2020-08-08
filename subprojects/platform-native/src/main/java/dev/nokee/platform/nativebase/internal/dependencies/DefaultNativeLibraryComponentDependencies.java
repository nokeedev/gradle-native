package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

import static dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx.configureAsBucket;

public class DefaultNativeLibraryComponentDependencies extends DefaultNativeComponentDependencies implements NativeLibraryComponentDependencies, ComponentDependencies {
	@Getter private final DependencyBucket api;

	@Inject
	public DefaultNativeLibraryComponentDependencies(ComponentDependenciesInternal delegate) {
		super(delegate);
		this.api = delegate.create("api", this::configureApiConfiguration);
	}

	private void configureApiConfiguration(Configuration configuration) {
		configureAsBucket(configuration);
		configuration.setDescription(String.format("API dependencies for %s.", getComponentDisplayName()));

		// Configure this here to simplify testing, it ends up being the same
		getImplementation().getAsConfiguration().extendsFrom(configuration);
	}

	@Override
	public void api(Object notation) {
		getApi().addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		getApi().addDependency(notation, action);
	}
}
