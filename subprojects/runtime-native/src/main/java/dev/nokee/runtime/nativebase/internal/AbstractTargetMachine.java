package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateAxis;
import dev.nokee.runtime.core.CoordinateTuple;
import dev.nokee.runtime.nativebase.MachineArchitecture;
import dev.nokee.runtime.nativebase.OperatingSystemFamily;
import dev.nokee.runtime.nativebase.TargetMachine;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.nokee.runtime.nativebase.MachineArchitecture.ARCHITECTURE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS;

@EqualsAndHashCode
abstract class AbstractTargetMachine implements TargetMachine, Coordinate<TargetMachine>, CoordinateTuple {
	private final Coordinate<MachineArchitecture> architectureCoordinate;
	private final Coordinate<OperatingSystemFamily> operatingSystemCoordinate;

	AbstractTargetMachine(OperatingSystemFamily operatingSystemFamily, MachineArchitecture architecture) {
		this.architectureCoordinate = Coordinate.of(ARCHITECTURE_COORDINATE_AXIS, architecture);
		this.operatingSystemCoordinate = Coordinate.of(OPERATING_SYSTEM_COORDINATE_AXIS, operatingSystemFamily);
	}

	@Override
	public OperatingSystemFamily getOperatingSystemFamily() {
		return operatingSystemCoordinate.getValue();
	}

	@Override
	public MachineArchitecture getArchitecture() {
		return architectureCoordinate.getValue();
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
