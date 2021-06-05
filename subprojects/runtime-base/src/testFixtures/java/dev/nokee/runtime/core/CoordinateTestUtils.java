package dev.nokee.runtime.core;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class CoordinateTestUtils {
	public static CoordinateAxis<TestAxis> testAxis() {
		return CoordinateAxis.of(TestAxis.class, "test");
	}

	public static CoordinateAxis<Long> xAxis() {
		return CoordinateAxis.of(Long.class, "x");
	}

	public static CoordinateAxis<Long> yAxis() {
		return CoordinateAxis.of(Long.class, "y");
	}

	public static CoordinateAxis<Long> zAxis() {
		return CoordinateAxis.of(Long.class, "z");
	}

	public static CoordinateTuple point(long x) {
		return new DefaultCoordinateTuple(singletonList(xAxis().create(x)));
	}

	public static CoordinateTuple point(long x, long y) {
		return new DefaultCoordinateTuple(asList(xAxis().create(x), yAxis().create(y)));
	}

	public static CoordinateTuple point(long x, long y, long z) {
		return new DefaultCoordinateTuple(asList(xAxis().create(x), yAxis().create(y), zAxis().create(z)));
	}

}
