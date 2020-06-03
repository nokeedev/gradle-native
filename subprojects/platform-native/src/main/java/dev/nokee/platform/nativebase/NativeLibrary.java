package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.Variant;

/**
 * A native library component that is built for a specific target platform.
 *
 * @since 0.4
 */
public interface NativeLibrary extends Variant, DependencyAwareComponent<NativeLibraryDependencies>, BinaryAwareComponent {
}
