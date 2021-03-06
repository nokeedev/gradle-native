package dev.nokee.language.swift;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.language.swift.internal.plugins.SwiftLanguageBasePlugin;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.scripts.testing.DefaultImporterMatchers.hasDefaultImportFor;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(SwiftLanguageBasePlugin.class)
class SwiftLanguageBasePluginTest {
	@Test
	void appliesLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", SwiftLanguageBasePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(LanguageBasePlugin.class), "should apply language base plugin");
	}

	@Test
	void defaultImportSourceSetTypes() {
		val project = rootProject();
		project.apply(singletonMap("plugin", SwiftLanguageBasePlugin.class));
		assertThat(project, hasDefaultImportFor(SwiftSourceSet.class));
	}
}
