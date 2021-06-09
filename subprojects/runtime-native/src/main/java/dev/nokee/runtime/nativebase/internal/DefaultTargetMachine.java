package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Value
@NonFinal /** Because the {@link DefaultTargetMachineFactory} creates a builder-like {@link TargetMachine} */
@AllArgsConstructor()
public class DefaultTargetMachine implements TargetMachine, Coordinate<TargetMachine>, CoordinateTuple {
	public static CoordinateAxis<TargetMachine> TARGET_MACHINE_COORDINATE_AXIS = CoordinateAxis.of(TargetMachine.class);
	@NonNull DefaultOperatingSystemFamily operatingSystemFamily;
	@NonNull DefaultMachineArchitecture architecture;

	public static Predicate<TargetMachine> isTargetingHost() {
		return targetMachine -> DefaultOperatingSystemFamily.HOST.equals(targetMachine.getOperatingSystemFamily()) && DefaultMachineArchitecture.HOST.equals(targetMachine.getArchitecture());
	}

	@Override
	public Iterator<Coordinate<?>> iterator() {
		return Stream.<Coordinate<?>>of(operatingSystemFamily, architecture).iterator();
	}

	@Override
	public CoordinateAxis<TargetMachine> getAxis() {
		return TARGET_MACHINE_COORDINATE_AXIS;
	}
}
