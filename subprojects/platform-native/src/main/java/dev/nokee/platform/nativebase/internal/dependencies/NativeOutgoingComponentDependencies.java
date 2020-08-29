package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.base.DependencyBucketName;
import lombok.Getter;

public class NativeOutgoingComponentDependencies {
	@Getter private final ConsumableNativeHeaders compileElements;
	@Getter private final ConsumableLinkLibraries linkElements;
	@Getter private final ConsumableRuntimeLibraries runtimeElements;

	public NativeOutgoingComponentDependencies(ComponentDependenciesContainer dependencies) {
		this.compileElements = dependencies.register(DependencyBucketName.of("compileElements"), ConsumableNativeHeaders.class);
		this.linkElements = dependencies.register(DependencyBucketName.of("linkElements"), ConsumableLinkLibraries.class);
		this.runtimeElements = dependencies.register(DependencyBucketName.of("runtimeElements"), ConsumableRuntimeLibraries.class);
	}
}
