package dev.nokee.language.jvm.internal.plugins;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import org.gradle.api.Plugin;
import spock.lang.Subject;

import static dev.nokee.internal.testing.Assumptions.skipCurrentTestExecution;

@Subject(JvmLanguageBasePlugin.class)
class JvmLanguageBaseWellBehavedPluginTest extends WellBehavedPluginTester {
	@Override
	protected String getQualifiedPluginIdUnderTest() {
		return skipCurrentTestExecution("plugin");
	}

	@Override
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		return JvmLanguageBasePlugin.class;
	}
}
