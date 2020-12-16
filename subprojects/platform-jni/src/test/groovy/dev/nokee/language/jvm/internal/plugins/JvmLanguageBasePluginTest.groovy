package dev.nokee.language.jvm.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.language.base.internal.LanguageSourceSetRepository
import dev.nokee.language.jvm.GroovySourceSet
import dev.nokee.language.jvm.JavaSourceSet
import dev.nokee.language.jvm.KotlinSourceSet
import dev.nokee.language.jvm.internal.GroovySourceSetImpl
import dev.nokee.language.jvm.internal.JavaSourceSetImpl
import dev.nokee.language.jvm.internal.KotlinSourceSetImpl
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.model.internal.TypeAwareDomainObjectIdentifier
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.ComponentContainer
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.util.function.Predicate

@Subject(JvmLanguageBasePlugin)
class JvmLanguageBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "does not register any JVM source set when no JVM plugins applied"() {
		when:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		then:
		project.extensions.getByType(LanguageSourceSetRepository).filter(forAllJvmSourceSet()) == [] as Set
	}

	def "registers Java source set to each component matching Gradle source set name"() {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		when:
		project.apply plugin: 'java'
		then:
		allJvmSourceSet.empty

		when:
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("main", MyComponent.class)
		then:
		def owner = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))
		allJvmSourceSet*.identifier == [sourceSetIdentifier('java', JavaSourceSetImpl, owner)]
	}

	def "registers Groovy source set to each component matching Gradle source set name"() {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		when:
		project.apply plugin: 'groovy'
		then:
		allJvmSourceSet.empty

		when:
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("main", MyComponent.class)
		then:
		def owner = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))
		allJvmSourceSet*.identifier == [sourceSetIdentifier('java', JavaSourceSetImpl, owner), sourceSetIdentifier('groovy', GroovySourceSetImpl, owner)]
	}

	def "registers Kotlin source set to each component matching Gradle source set name"() {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		when:
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		then:
		allJvmSourceSet.empty

		when:
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("main", MyComponent.class)
		then:
		def owner = ComponentIdentifier.ofMain(MyComponent, ProjectIdentifier.of(project))
		allJvmSourceSet*.identifier == [sourceSetIdentifier('java', JavaSourceSetImpl, owner), sourceSetIdentifier('kotlin', KotlinSourceSetImpl, owner)]
	}

	@Unroll
	def "does not register source set when no matching component to Gradle source set name"(pluginIdsUnderTest) {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		when:
		project.apply plugin: pluginIdsUnderTest
		then:
		allJvmSourceSet.empty

		when:
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("integTest", MyComponent.class)
		then:
		allJvmSourceSet.empty

		where:
		pluginIdsUnderTest << ['java', 'groovy', 'org.jetbrains.kotlin.jvm']
	}

	def "registers Java source set upon new Gradle source set matches component by name"() {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		and:
		project.apply plugin: 'java'
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("integTest", MyComponent.class)

		when:
		project.sourceSets.create('integTest')

		then:
		def owner = ComponentIdentifier.of(ComponentName.of('integTest'), MyComponent, ProjectIdentifier.of(project))
		allJvmSourceSet*.identifier == [sourceSetIdentifier('java', JavaSourceSetImpl, owner)]
	}

	def "registers Groovy source set upon new Gradle source set matches component by name"() {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		and:
		project.apply plugin: 'groovy'
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("integTest", MyComponent.class)

		when:
		project.sourceSets.create('integTest')

		then:
		def owner = ComponentIdentifier.of(ComponentName.of('integTest'), MyComponent, ProjectIdentifier.of(project))
		allJvmSourceSet*.identifier == [sourceSetIdentifier('java', JavaSourceSetImpl, owner), sourceSetIdentifier('groovy', GroovySourceSetImpl, owner)]
	}

	def "registers Kotlin source set upon new Gradle source set matches component by name"() {
		given:
		project.apply plugin: JvmLanguageBasePlugin
		project.apply plugin: ComponentModelBasePlugin

		and:
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		project.extensions.getByType(ComponentContainer).registerFactory(MyComponent, { new MyComponent() })
		project.extensions.getByType(ComponentContainer).register("integTest", MyComponent.class)

		when:
		project.sourceSets.create('integTest')

		then:
		def owner = ComponentIdentifier.of(ComponentName.of('integTest'), MyComponent, ProjectIdentifier.of(project))
		allJvmSourceSet*.identifier == [sourceSetIdentifier('java', JavaSourceSetImpl, owner), sourceSetIdentifier('kotlin', KotlinSourceSetImpl, owner)]
	}

	private <T extends LanguageSourceSet> LanguageSourceSetIdentifier<T> sourceSetIdentifier(String name, Class<T> type, DomainObjectIdentifier owner) {
		return LanguageSourceSetIdentifier.of(LanguageSourceSetName.of(name), type, owner)
	}

	private Set<LanguageSourceSet> getAllJvmSourceSet() {
		return project.extensions.getByType(LanguageSourceSetRepository).filter(forAllJvmSourceSet())
	}

	private static <T extends LanguageSourceSet> Predicate<? super TypeAwareDomainObjectIdentifier<T>> forAllJvmSourceSet() {
		return { GroovySourceSet.isAssignableFrom(it.type) || JavaSourceSet.isAssignableFrom(it.type) || KotlinSourceSet.isAssignableFrom(it.type) }
	}

	static class MyComponent implements Component {}
}
