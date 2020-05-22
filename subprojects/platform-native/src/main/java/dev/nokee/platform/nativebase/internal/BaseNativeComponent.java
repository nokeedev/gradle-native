package dev.nokee.platform.nativebase.internal;

public abstract class BaseNativeComponent {
	private final DefaultNativeComponentDependencies dependencies;

	public BaseNativeComponent(DefaultNativeComponentDependencies dependencies) {
		this.dependencies = dependencies;
	}

	public DefaultNativeComponentDependencies getDependencies() {
		return dependencies;
	}

	// Not every implementation needs this, however, the public type may not expose it.
	public DefaultTargetMachineFactory getMachines() {
		return DefaultTargetMachineFactory.INSTANCE;
	}
}
