package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
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
	private static final MachineArchitecture HOST_ARCH = MachineArchitecture.forName(System.getProperty("os.arch"));
	private static final OperatingSystemFamily HOST_OS = OperatingSystemFamily.forName(System.getProperty("os.name"));
	@NonNull OperatingSystemFamily operatingSystemFamily;
	@NonNull MachineArchitecture architecture;
	private final Coordinate<MachineArchitecture> architectureCoordinate;
	private final Coordinate<OperatingSystemFamily> operatingSystemCoordinate;

	DefaultTargetMachine(OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
		this.operatingSystemFamily = operatingSystemFamily;
		this.architecture = architecture;
		this.architectureCoordinate = Coordinate.of(MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS, architecture);
		this.operatingSystemCoordinate = Coordinate.of(OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS, operatingSystemFamily);
	}

	public static Predicate<TargetMachine> isTargetingHost() {
		return targetMachine -> HOST_OS.equals(targetMachine.getOperatingSystemFamily()) && HOST_ARCH.equals(targetMachine.getArchitecture());
	}

	@Override
	public Iterator<Coordinate<?>> iterator() {
		return Stream.<Coordinate<?>>of(operatingSystemCoordinate, architectureCoordinate).iterator();
	}

	@Override
	public CoordinateAxis<TargetMachine> getAxis() {
		return TARGET_MACHINE_COORDINATE_AXIS;
	}
}
