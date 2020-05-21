package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import org.gradle.api.Action;

public abstract class BaseNativeLibraryComponent extends BaseNativeComponent {
	public BaseNativeLibraryComponent(DefaultNativeLibraryDependencies dependencies) {
		super(dependencies);
	}

	@Override
	public DefaultNativeLibraryDependencies getDependencies() {
		return (DefaultNativeLibraryDependencies)super.getDependencies();
	}

	public void dependencies(Action<? super NativeLibraryDependencies> action) {
		action.execute(getDependencies());
	}
}
