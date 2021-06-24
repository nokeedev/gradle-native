package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetLinkageFactory;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import org.gradle.api.provider.SetProperty;

/**
 * Represents a component that targets different library linkage.
 *
 * @since 0.5
 */
public interface TargetLinkageAwareComponent {
	/**
	 * Specifies the target linkage this component should be built for.
	 * The {@link #getLinkages()} property (see {@link TargetLinkageFactory}) can be used to construct common linkages.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetLinkages = [linkages.shared, linkages.static]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetLinkage}, never null.
	 */
	SetProperty<TargetLinkage> getTargetLinkages();

	/**
	 * Returns a factory to create target linkages when configuring {@link #getTargetLinkages()}.
	 *
	 * @return a {@link TargetLinkageFactory} for creating {@link TargetLinkage} instance, never null.
	 */
	default TargetLinkageFactory getLinkages() {
		return NativeRuntimeBasePlugin.TARGET_LINKAGE_FACTORY;
	}
}
