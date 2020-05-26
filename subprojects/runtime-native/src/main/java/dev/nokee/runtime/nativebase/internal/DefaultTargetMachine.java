package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.function.Predicate;

@Value
@NonFinal /** Because the {@link DefaultTargetMachineFactory} creates a builder-like {@link TargetMachine} */
@AllArgsConstructor()
public class DefaultTargetMachine implements TargetMachine {
	@NonNull DefaultOperatingSystemFamily operatingSystemFamily;
	@NonNull DefaultMachineArchitecture architecture;

	public static Predicate<TargetMachine> isTargetingHost() {
		return targetMachine -> DefaultOperatingSystemFamily.HOST.equals(targetMachine.getOperatingSystemFamily()) && DefaultMachineArchitecture.HOST.equals(targetMachine.getArchitecture());
	}
}
