package dev.nokee.platform.nativebase.internal;

public abstract class BaseNativeComponent {
	private final DefaultNativeComponentDependencies dependencies;

	public BaseNativeComponent(DefaultNativeComponentDependencies dependencies) {
		this.dependencies = dependencies;
	}

	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}
}
