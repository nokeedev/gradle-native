package dev.nokee.language.c;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.nativebase.NativeHeaderSet;
import lombok.val;
import org.gradle.api.Plugin;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.Assumptions.skipCurrentTestExecution;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.scripts.testing.DefaultImporterMatchers.hasDefaultImportFor;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(CLanguageBasePlugin.class)
class CLanguageBasePluginTest extends WellBehavedPluginTester {
	@Override
	protected String getQualifiedPluginIdUnderTest() {
		return skipCurrentTestExecution("no qualified plugin id");
	}

	@Override
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		return CLanguageBasePlugin.class;
	}

	@Test
	void appliesLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", CLanguageBasePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(LanguageBasePlugin.class), "should apply language base plugin");
	}

	@Test
	void defaultImportSourceSetTypes() {
		val project = rootProject();
		project.apply(singletonMap("plugin", CLanguageBasePlugin.class));
		assertThat(project, hasDefaultImportFor(CSourceSet.class));
		assertThat(project, hasDefaultImportFor(CHeaderSet.class));
		assertThat(project, hasDefaultImportFor(NativeHeaderSet.class));
	}
}
