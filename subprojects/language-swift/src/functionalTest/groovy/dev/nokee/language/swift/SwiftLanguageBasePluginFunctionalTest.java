package dev.nokee.language.swift;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class SwiftLanguageBasePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.pluginClass(SwiftLanguageBasePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
