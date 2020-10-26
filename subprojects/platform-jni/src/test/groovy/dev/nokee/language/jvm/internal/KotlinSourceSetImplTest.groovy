package dev.nokee.language.jvm.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.jvm.KotlinSourceSet
import spock.lang.Subject

@Subject(KotlinSourceSetImpl)
class KotlinSourceSetImplTest extends AbstractLanguageSourceSetTest<KotlinSourceSet> {

	def setup() {
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
	}

	@Override
	protected KotlinSourceSet newSubject() {
		return new KotlinSourceSetImpl(newIdentifier(), project.sourceSets.main.kotlin, project.objects)
	}

	@Override
	protected Class<KotlinSourceSet> getPublicType() {
		return KotlinSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.java', '**/*.kt', '**/*.kts']
	}

	@Override
	protected String fileName(String fileName) {
		return "${fileName}.kt"
	}
}
