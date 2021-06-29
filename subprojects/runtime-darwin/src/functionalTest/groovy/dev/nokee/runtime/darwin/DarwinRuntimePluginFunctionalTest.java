package dev.nokee.runtime.darwin;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.runtime.darwin.internal.DarwinRuntimePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class DarwinRuntimePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.darwin-runtime")
			.pluginClass(DarwinRuntimePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
