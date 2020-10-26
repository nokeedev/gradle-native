package dev.nokee.language.jvm.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.jvm.GroovySourceSet
import org.gradle.api.plugins.GroovyPlugin
import spock.lang.Subject

@Subject(GroovySourceSetImpl)
class GroovySourceSetImplTest extends AbstractLanguageSourceSetTest<GroovySourceSet> {

	def setup() {
		project.apply plugin: GroovyPlugin
	}

	@Override
	protected GroovySourceSet newSubject() {
		return new GroovySourceSetImpl(newIdentifier(), project.sourceSets.main.groovy, project.objects)
	}

	@Override
	protected Class<GroovySourceSet> getPublicType() {
		return GroovySourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.java', '**/*.groovy'] // Baggage from Gradle
	}

	@Override
	protected String fileName(String fileName) {
		return "${fileName}.groovy"
	}
}
