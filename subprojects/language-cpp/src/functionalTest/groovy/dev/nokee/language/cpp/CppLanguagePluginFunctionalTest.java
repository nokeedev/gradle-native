package dev.nokee.language.cpp;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.cpp.internal.plugins.CppLanguagePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class CppLanguagePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.cpp-language")
			.pluginClass(CppLanguagePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
