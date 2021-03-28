package dev.nokee.language.base;

import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.scripts.testing.DefaultImporterMatchers.hasDefaultImportFor;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(LanguageBasePlugin.class)
class LanguageBasePluginTest {
	@Test
	void appliesLanguageBasePlugin() {
		val project = rootProject();
		project.apply(singletonMap("plugin", LanguageBasePlugin.class));
		assertTrue(project.getPlugins().hasPlugin(ModelBasePlugin.class), "should apply model base plugin");
	}

	@Test
	void defaultImportSourceSetTypes() {
		val project = rootProject();
		project.apply(singletonMap("plugin", LanguageBasePlugin.class));
		assertThat(project, hasDefaultImportFor(LanguageSourceSet.class));
	}

	/*
		def "applies source set convention rule to all source set"() {
		given:
		project.apply plugin: LanguageBasePlugin
		def registry = project.extensions.getByType(LanguageSourceSetRegistry)

		and:
		def file1 = file('src/main/c/foo')
		def file2 = file('src/test/cpp/foo')

		when:
		def sourceSet1 = registry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('c'), LanguageSourceSetImpl, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of(project))))
		def sourceSet2 = registry.create(LanguageSourceSetIdentifier.of(LanguageSourceSetName.of('cpp'), LanguageSourceSetImpl, ComponentIdentifier.of(ComponentName.of('test'), Component, ProjectIdentifier.of(project))))

		then:
		sourceSet1.asFileTree.files == [file1] as Set
		sourceSet2.asFileTree.files == [file2] as Set
	}
	 */
}
