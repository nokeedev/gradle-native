package dev.nokee.platform.swift;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.*;

/**
 * Configuration for a library written in Swift, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Library Plugin.</p>
 *
 * @since 0.4
 */
@Deprecated // Use SwiftLibrary instead
public interface SwiftLibraryExtension extends DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<SwiftLibrarySources>, HasSwiftSources {}
