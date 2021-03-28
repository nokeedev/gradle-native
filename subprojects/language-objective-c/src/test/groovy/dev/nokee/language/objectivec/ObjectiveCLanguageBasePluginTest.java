package dev.nokee.language.objectivec;

import dev.nokee.internal.testing.testers.WellBehavedPluginTester;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguageBasePlugin;
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

@Subject(ObjectiveCLanguageBasePlugin.class)
class ObjectiveCLanguageBasePluginTest extends WellBehavedPluginTester {
	@Override
	protected String getQualifiedPluginIdUnderTest() {
		return skipCurrentTestExecution("no qualified plugin id");
	}

	@Override
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		return ObjectiveCLanguageBasePlugin.class;
	}

	@Test
	void appliesLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCLanguageBasePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(LanguageBasePlugin.class), "should apply language base plugin");
	}

	@Test
	void defaultImportSourceSetTypes() {
		val project = rootProject();
		project.apply(singletonMap("plugin", ObjectiveCLanguageBasePlugin.class));
		assertThat(project, hasDefaultImportFor(ObjectiveCSourceSet.class));
		assertThat(project, hasDefaultImportFor(CHeaderSet.class));
		assertThat(project, hasDefaultImportFor(NativeHeaderSet.class));
	}
}
