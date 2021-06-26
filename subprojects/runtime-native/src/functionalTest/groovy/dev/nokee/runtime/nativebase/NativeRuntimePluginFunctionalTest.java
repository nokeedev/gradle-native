package dev.nokee.runtime.nativebase;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.runtime.nativebase.internal.NativeRuntimePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class NativeRuntimePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.native-runtime")
			.pluginClass(NativeRuntimePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
