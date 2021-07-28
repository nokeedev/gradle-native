package dev.nokee.runtime.nativebase.internal;

import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.host;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.isTargetingHost;
import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assertions.*;

class TargetMachines_IsTargetingHostTest {
	@Test
	void returnsTrueForHostInstance() {
		assertTrue(isTargetingHost(host()));
	}

	@Test
	void returnsTrueForRenamedHostInstance() {
		assertTrue(isTargetingHost(host().named("foo")));
	}

	@Test
	void returnsTrueForEquivalentHostTargetMachine() {
		assertAll(
			() -> assertTrue(isTargetingHost(TARGET_MACHINE_FACTORY.os(getProperty("os.name")))),
			() -> assertTrue(isTargetingHost(TARGET_MACHINE_FACTORY.os(getProperty("os.name")).architecture(getProperty("os.arch"))))
		);
	}

	@Test
	void returnsFalseForNonHostTargetMachine() {
		assertAll(
			() -> assertFalse(isTargetingHost(TARGET_MACHINE_FACTORY.os("non-host"))),
			() -> assertFalse(isTargetingHost(TARGET_MACHINE_FACTORY.os("non-host").architecture("non-host"))),
			() -> assertFalse(isTargetingHost(TARGET_MACHINE_FACTORY.os(getProperty("os.name")).architecture("non-host")))
		);
	}
}
