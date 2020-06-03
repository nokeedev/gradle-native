package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.Variant;

/**
 * A native application variant that is built for a specific target platform.
 *
 * @since 0.4
 */
public interface NativeApplication extends Variant, DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent {
}
