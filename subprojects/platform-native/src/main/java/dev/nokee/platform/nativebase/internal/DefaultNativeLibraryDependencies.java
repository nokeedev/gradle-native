package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryDependencies extends DefaultNativeComponentDependencies implements NativeLibraryDependencies {
	private final DependencyBucket api;

	@Inject
	public DefaultNativeLibraryDependencies(NamingScheme names) {
		super(names);
		this.api = getObjects().newInstance(NativeDependencyBucket.class, getConfigurations().create("api", ConfigurationUtils::configureAsBucket));
	}

	@Override
	public void api(Object notation) {
		api.addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		api.addDependency(notation, action);
	}
}
