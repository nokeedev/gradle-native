package dev.nokee.platform.swift;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import org.gradle.api.file.ConfigurableFileCollection;

/**
 * Configuration for a library written in Swift, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Swift Library Plugin.</p>
 *
 * @since 0.4
 */
public interface SwiftLibraryExtension extends DependencyAwareComponent<NativeLibraryDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent {
	/**
	 * Defines the source files or directories of this library.
	 * You can add files or directories to this collection.
	 * When a directory is added, all source files are included for compilation.
	 *
	 * <p>When this collection is empty, the directory {@code src/main/swift} is used by default.</p>
	 *
	 * @since 0.5
	 */
	ConfigurableFileCollection getSources();
}
