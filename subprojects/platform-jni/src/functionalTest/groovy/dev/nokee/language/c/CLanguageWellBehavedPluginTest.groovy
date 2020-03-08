package dev.nokee.language.c

import dev.gradleplugins.integtests.fixtures.WellBehavedPluginTest

class CLanguageWellBehavedPluginTest extends WellBehavedPluginTest {
	@Override
	String getQualifiedPluginId() {
		return 'dev.nokee.c-language'
	}
}
