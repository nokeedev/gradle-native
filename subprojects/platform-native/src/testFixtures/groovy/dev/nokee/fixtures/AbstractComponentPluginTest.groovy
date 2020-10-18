package dev.nokee.fixtures

import dev.nokee.language.base.LanguageSourceSet
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier
import dev.nokee.language.base.internal.LanguageSourceSetName
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.SourceAwareComponent
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.HasLanguageSourceSetAccessor
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assume
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractComponentPluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	@Unroll
	def "extension has default source set getter"(expectedSourceSet) {
		given:
		applyPluginUnderTests(project)
		Assume.assumeTrue(getExtension(project, extensionTypeUnderTest) instanceof HasLanguageSourceSetAccessor)

		expect:
		def sourceSet = project.extensions.getByType(extensionTypeUnderTest)."${expectedSourceSet.getterName}"
		expectedSourceSet.type.isInstance(sourceSet)

		and: 'source set owned by main component'
		sourceSet.identifier.ownerIdentifier.name == ComponentName.of('main')
		sourceSet.identifier.ownerIdentifier.type == componentTypeUnderTest

		where:
		expectedSourceSet << expectedLanguageSourceSets
	}

	@Unroll
	def "extension has default source set configuration method"(expectedSourceSet) {
		given:
		applyPluginUnderTests(project)
		Assume.assumeTrue(getExtension(project, extensionTypeUnderTest) instanceof HasLanguageSourceSetAccessor)

		expect:
		def sourceSet = null
		getExtension(project, extensionTypeUnderTest)."${expectedSourceSet.getterName}"(new Action() {
			@Override
			void execute(Object o) {
				sourceSet = o
			}
		})
		expectedSourceSet.type.isInstance(sourceSet)

		and: 'source set owned by main component'
		sourceSet.identifier.ownerIdentifier.name == ComponentName.of('main')
		sourceSet.identifier.ownerIdentifier.type == componentTypeUnderTest

		and: 'source set has conventional source layout'
		sourceSet.sourceDirectories.files as Set == expectedSourceSet.conventionDirectories.collect { project.file(it) } as Set

		where:
		expectedSourceSet << expectedLanguageSourceSets
	}

	def "extension accessible by name"() {
		given:
		applyPluginUnderTests(project)

		expect:
		def extension = project.extensions.findByName(extensionNameUnderTest)
		extensionTypeUnderTest.isInstance(extension)
	}

	def "extension has source set view containing all default source sets"() {
		Assume.assumeTrue(SourceAwareComponent.isAssignableFrom(extensionTypeUnderTest))

		given:
		applyPluginUnderTests(project)

		expect:
		getExtension(project, extensionTypeUnderTest).sources.get()*.identifier == expectedLanguageSourceSets*.identifier
	}

	def "extension has source set view for component"() {
		Assume.assumeTrue(SourceAwareComponent.isAssignableFrom(extensionTypeUnderTest))

		given:
		applyPluginUnderTests(project)

		expect:
		getExtension(project, extensionTypeUnderTest).sources.viewOwner == ComponentIdentifier.ofMain(componentTypeUnderTest, ProjectIdentifier.of(project))
		getExtension(project, extensionTypeUnderTest).sources.viewElementType == LanguageSourceSet
	}

	private static <T> T getExtension(Project project, Class<T> type) {
		return project.extensions.getByType(type)
	}

	protected String getExtensionNameUnderTest() {
		if (extensionTypeUnderTest.simpleName.contains('Application')) {
			return 'application'
		} else if (extensionTypeUnderTest.simpleName.contains('Library')) {
			return 'library'
		}
		throw new UnsupportedOperationException('Unable to figure out the extension under test name')
	}

	protected abstract Class getExtensionTypeUnderTest()

	protected abstract Class getComponentTypeUnderTest()

	protected abstract void applyPluginUnderTests(Project project)

	protected abstract List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets()

	protected ExpectedLanguageSourceSet newExpectedSourceSet(String name, Class type, String getterName = "${name}Sources") {
		return new ExpectedLanguageSourceSet() {
			private final List<String> conventionDirectories = ["src/main/${name}".toString()]
			@Override
			Class getType() {
				return type
			}

			@Override
			String getName() {
				return name
			}

			@Override
			String getGetterName() {
				return getterName
			}

			@Override
			LanguageSourceSetIdentifier getIdentifier() {
				return LanguageSourceSetIdentifier.of(LanguageSourceSetName.of(name), type, ComponentIdentifier.ofMain(componentTypeUnderTest, ProjectIdentifier.of(project)))
			}

			@Override
			List<String> getConventionDirectories() {
				return conventionDirectories
			}

			ExpectedLanguageSourceSet addConventionDirectory(String dir) {
				conventionDirectories.add(dir)
				return this
			}
		}
	}

	interface ExpectedLanguageSourceSet {
		Class getType()
		String getName()
		String getGetterName()
		LanguageSourceSetIdentifier getIdentifier()
		List<String> getConventionDirectories()
		ExpectedLanguageSourceSet addConventionDirectory(String dir)
	}
}
