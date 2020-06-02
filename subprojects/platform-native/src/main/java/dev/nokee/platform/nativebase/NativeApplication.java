package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.Variant;

/**
 * A native application variant that is built for a specific target platform.
 *
 * @since 4.2
 */
public interface NativeApplication extends Variant, DependencyAwareComponent<NativeComponentDependencies> {
}
