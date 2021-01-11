package dev.nokee.platform.swift;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;

/**
 * Configuration for a library written in Swift, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Library Plugin.</p>
 *
 * @since 0.5
 */
public interface SwiftLibrary extends SwiftLibraryExtension, DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<SwiftLibrarySources>, HasSwiftSources, BaseNameAwareComponent {
	/**
	 * {@inheritDoc}
	 */
	default NativeLibraryComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeLibraryComponent.class).getDependencies();
	}
}
