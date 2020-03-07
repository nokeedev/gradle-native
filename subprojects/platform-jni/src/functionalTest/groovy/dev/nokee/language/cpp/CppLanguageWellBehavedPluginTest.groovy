package dev.nokee.language.cpp

import dev.gradleplugins.integtests.fixtures.WellBehavedPluginTest

class CppLanguageWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	String getQualifiedPluginId() {
		return 'dev.nokee.cpp-language'
	}
}
