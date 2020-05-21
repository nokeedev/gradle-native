package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.Action;

public abstract class BaseNativeApplicationComponent extends BaseNativeComponent {

	public BaseNativeApplicationComponent(DefaultNativeComponentDependencies dependencies) {
		super(dependencies);
	}

	public void dependencies(Action<? super NativeComponentDependencies> action) {
		action.execute(getDependencies());
	}
}
