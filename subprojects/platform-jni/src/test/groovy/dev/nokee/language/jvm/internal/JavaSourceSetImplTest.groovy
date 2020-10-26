package dev.nokee.language.jvm.internal

import dev.nokee.language.base.AbstractLanguageSourceSetTest
import dev.nokee.language.jvm.JavaSourceSet
import org.gradle.api.plugins.JavaPlugin
import org.junit.Ignore
import spock.lang.Subject

@Ignore
@Subject(JavaSourceSetImpl)
class JavaSourceSetImplTest extends AbstractLanguageSourceSetTest<JavaSourceSet> {

	def setup() {
		project.apply plugin: JavaPlugin
	}

	@Override
	protected JavaSourceSet newSubject() {
		return new JavaSourceSetImpl(newIdentifier(), project.sourceSets.main.java, project.objects)
	}

	@Override
	protected Class<JavaSourceSet> getPublicType() {
		return JavaSourceSet
	}

	@Override
	protected Set<String> getDefaultFilterIncludes() {
		return ['**/*.java']
	}

	@Override
	protected String fileName(String fileName) {
		return "${fileName}.java"
	}
}
