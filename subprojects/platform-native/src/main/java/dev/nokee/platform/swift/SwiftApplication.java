package dev.nokee.platform.swift;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;

/**
 * Configuration for an application written in Swift, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Application Plugin.</p>
 *
 * @since 0.5
 */
public interface SwiftApplication extends SwiftApplicationExtension, DependencyAwareComponent<NativeApplicationComponentDependencies>, VariantAwareComponent<NativeApplication>, BinaryAwareComponent, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<SwiftApplicationSources>, HasSwiftSources, BaseNameAwareComponent {
	default NativeApplicationComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeApplicationComponent.class).getDependencies();
	}
}
