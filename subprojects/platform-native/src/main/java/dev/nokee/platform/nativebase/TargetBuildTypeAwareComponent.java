package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetBuildTypeFactory;
import dev.nokee.runtime.nativebase.internal.RuntimeNativePlugin;
import org.gradle.api.provider.SetProperty;

/**
 * Represents a component that targets different build type.
 *
 * @since 0.5
 */
public interface TargetBuildTypeAwareComponent {
	/**
	 * Specifies the target build type this component should be built for.
	 * The {@link #getBuildTypes()} property (see {@link TargetBuildTypeFactory}) can be used to construct common build types.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetBuildTypes = [buildTypes.named('debug'), buildTypes.named('release')]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetBuildType}, never null.
	 */
	SetProperty<TargetBuildType> getTargetBuildTypes();

	/**
	 * Returns a factory to create target linkages when configuring {@link #getTargetBuildTypes()}.
	 *
	 * @return a {@link TargetBuildTypeFactory} for creating {@link TargetBuildType} instance, never null.
	 */
	default TargetBuildTypeFactory getBuildTypes() {
		return RuntimeNativePlugin.TARGET_BUILD_TYPE_FACTORY;
	}
}
