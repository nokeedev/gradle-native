package dev.nokee.ide.xcode.internal.plugins

import dev.nokee.ide.xcode.XcodeIdeProductType
import dev.nokee.ide.xcode.XcodeIdeProductTypes
import dev.nokee.ide.xcode.XcodeIdeProjectExtension
import dev.nokee.ide.xcode.XcodeIdeWorkspaceExtension
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(XcodeIdePlugin)
class XcodeIdePluginTest extends Specification {
	def project = ProjectBuilder.builder().withName('app').build()

	def "creates lifecycle task"() {
		when:
		project.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.tasks.xcode.group == XcodeIdePlugin.IDE_GROUP_NAME
		project.tasks.xcode.description == "Generates Xcode IDE configuration"
	}

	def "creates clean task"() {
		when:
		project.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.tasks.cleanXcode.group == XcodeIdePlugin.IDE_GROUP_NAME
		project.tasks.cleanXcode.description == "Cleans Xcode IDE configuration"
	}

	def "creates open task only on root project"() {
		when:
		project.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.tasks.openXcode.group == XcodeIdePlugin.IDE_GROUP_NAME
		project.tasks.openXcode.description == "Opens the Xcode workspace"

		when:
		def subproject = ProjectBuilder.builder().withParent(project).build()
		subproject.apply plugin: 'dev.nokee.xcode-ide'

		then:
		subproject.tasks.findByName('openXcode') == null
	}

	def "registers extension on project"() {
		given:
		def subproject = ProjectBuilder.builder().withParent(project).build()

		when:
		project.apply plugin: 'dev.nokee.xcode-ide'
		subproject.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.xcode != null
		project.xcode instanceof XcodeIdeWorkspaceExtension

		and:
		subproject.xcode != null
		subproject.xcode instanceof XcodeIdeProjectExtension
		!(subproject.xcode instanceof XcodeIdeWorkspaceExtension)
	}

	def "registers empty extensions on project"() {
		when:
		project.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.xcode.workspace.displayName == 'Xcode workspace'
		project.xcode.workspace.projects.get() == [] as Set
		project.xcode.workspace.location.get().asFile.absolutePath == "${project.projectDir}/${project.name}.xcworkspace"

		and:
		project.xcode.projects == [] as Set
	}

	def "can access product types via repository getter from the project"() {
		given:
		project.apply plugin: 'dev.nokee.xcode-ide'

		expect:
		project.xcode.productTypes.application == XcodeIdeProductTypes.APPLICATION
		project.xcode.productTypes.dynamicLibrary == XcodeIdeProductTypes.DYNAMIC_LIBRARY
		project.xcode.productTypes.staticLibrary == XcodeIdeProductTypes.STATIC_LIBRARY
		project.xcode.productTypes.tool == XcodeIdeProductTypes.TOOL
		project.xcode.productTypes.of('com.acme.product-type.foo') == XcodeIdeProductType.of('com.acme.product-type.foo')
	}

	def "can access product types via repository getter from the project from within the DSL"() {
		given:
		project.apply plugin: 'dev.nokee.xcode-ide'

		when:
		project.xcode {
			productTypes.application // can access the repository from inside the extension
			projects.create('foo') {
				productTypes.application // can access the repository from the project
				targets.create('Foo') {
					productType = productTypes.application // can access the repository from target
					buildConfigurations.create('Default') {
						productTypes.application // can access the repository from build configurations
					}
				}
			}
		}

		then:
		noExceptionThrown()

		and:
		project.xcode.projects.foo.targets.Foo.productType.get() == XcodeIdeProductTypes.APPLICATION
	}

	def "does not register workspace task on subproject"() {
		given:
		def subproject = ProjectBuilder.builder().withParent(project).build()

		when:
		project.apply plugin: 'dev.nokee.xcode-ide'
		subproject.apply plugin: 'dev.nokee.xcode-ide'

		then:
		project.tasks.findByName('xcodeWorkspace') != null
		subproject.tasks.findByName('xcodeWorkspace') == null
	}
}
