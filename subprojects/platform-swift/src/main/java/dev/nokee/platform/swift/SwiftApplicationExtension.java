package dev.nokee.platform.swift;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

/**
 * Configuration for an application written in Swift, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Application Plugin.</p>
 *
 * @since 0.4
 */
@Deprecated // Use SwiftApplication instead
public interface SwiftApplicationExtension extends DependencyAwareComponent<NativeApplicationComponentDependencies>, VariantAwareComponent<NativeApplication>, BinaryAwareComponent, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<SwiftApplicationSources>, HasSwiftSources {}
