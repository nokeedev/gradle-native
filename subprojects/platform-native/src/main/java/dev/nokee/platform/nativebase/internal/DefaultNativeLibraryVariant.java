package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BuildVariant;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.BinaryAwareNativeLibraryDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.file.ProjectLayout;

import javax.inject.Inject;

public abstract class DefaultNativeLibraryVariant extends BaseNativeVariant implements NativeLibrary {
	@Getter private final BinaryAwareNativeLibraryDependencies dependencies;

	@Inject
	public DefaultNativeLibraryVariant(String name, NamingScheme names, BuildVariant buildVariant, BinaryAwareNativeLibraryDependencies dependencies) {
		super(name, names, buildVariant);
		this.dependencies = dependencies;
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	@Override
	public void dependencies(Action<? super NativeLibraryDependencies> action) {
		action.execute(dependencies);
	}
}
