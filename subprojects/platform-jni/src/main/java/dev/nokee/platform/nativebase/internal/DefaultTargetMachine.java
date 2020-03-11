package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.nativebase.TargetMachine;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.function.Predicate;

@Value
@NonFinal /** Because the {@link DefaultTargetMachineFactory} creates a builder-like {@link TargetMachine} */
@AllArgsConstructor(access = AccessLevel.PACKAGE) /** Use {@link TargetMachineFactory} instead */
public class DefaultTargetMachine implements TargetMachine {
	@NonNull DefaultOperatingSystemFamily operatingSystemFamily;
	@NonNull DefaultMachineArchitecture architecture;

	public static Predicate<TargetMachine> isTargetingHost() {
		return targetMachine -> DefaultOperatingSystemFamily.HOST.equals(targetMachine.getOperatingSystemFamily()) && DefaultMachineArchitecture.HOST.equals(targetMachine.getArchitecture());
	}
}
