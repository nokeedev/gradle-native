package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.provider.SetProperty;

/**
 * Represents a component that targets multiple target machines.
 * @since 0.4
 */
public interface TargetMachineAwareComponent {
	/**
	 * Specifies the target machines this component should be built for.
	 * The {@link #getMachines()} property (see {@link TargetMachineFactory}) can be used to construct common operating system and architecture combinations.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetMachines = [machines.linux.x86_64, machines.windows.x86_64]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetMachine}, never null.
	 */
	SetProperty<TargetMachine> getTargetMachines();

	/**
	 * Returns a factory to create target machines when configuring {@link #getTargetMachines()}.
	 *
	 * @return a {@link TargetMachineFactory} for creating {@link TargetMachine} instance, never null.
	 */
	TargetMachineFactory getMachines();
}
