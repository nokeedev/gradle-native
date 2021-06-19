package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.MachineArchitecture;
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
	private static final MachineArchitecture HOST = MachineArchitecture.forName(System.getProperty("os.arch"));
	@NonNull DefaultOperatingSystemFamily operatingSystemFamily;
	@NonNull MachineArchitecture architecture;
	private final Coordinate<MachineArchitecture> architectureCoordinate;

	DefaultTargetMachine(DefaultOperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
		this.operatingSystemFamily = operatingSystemFamily;
		this.architecture = architecture;
		this.architectureCoordinate = Coordinate.of(MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS, architecture);
	}

	public static Predicate<TargetMachine> isTargetingHost() {
		return targetMachine -> DefaultOperatingSystemFamily.HOST.equals(targetMachine.getOperatingSystemFamily()) && HOST.equals(targetMachine.getArchitecture());
	}

	@Override
	public Iterator<Coordinate<?>> iterator() {
		return Stream.<Coordinate<?>>of(operatingSystemFamily, architectureCoordinate).iterator();
	}

	@Override
	public CoordinateAxis<TargetMachine> getAxis() {
		return TARGET_MACHINE_COORDINATE_AXIS;
	}
}
