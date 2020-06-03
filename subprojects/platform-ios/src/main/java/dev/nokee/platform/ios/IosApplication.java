package dev.nokee.platform.ios;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.NativeComponentDependencies;

/**
 * A iOS application that is built for a specific target platform.
 *
 * @since 0.4
 */
public interface IosApplication extends Variant, DependencyAwareComponent<NativeComponentDependencies>, BinaryAwareComponent {
}
