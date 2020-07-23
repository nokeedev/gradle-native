package dev.nokee.buildadapter.cmake.internal;

import java.util.function.Supplier;

public class DeferUtils {
	public static Object asToStringObject(Supplier<String> supplier) {
		return new Object() {
			@Override
			public String toString() {
				return supplier.get();
			}
		};
	}
}
