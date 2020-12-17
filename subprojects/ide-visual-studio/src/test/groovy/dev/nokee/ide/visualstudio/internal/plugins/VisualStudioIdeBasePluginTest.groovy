package dev.nokee.ide.visualstudio.internal.plugins

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectExtension
import dev.nokee.ide.visualstudio.VisualStudioIdeWorkspaceExtension
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.ide.base.internal.plugins.AbstractIdePlugin.IDE_GROUP_NAME
import static org.apache.commons.io.FilenameUtils.separatorsToSystem

@Subject(VisualStudioIdeBasePlugin)
class VisualStudioIdeBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "registers lifecycle task"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'

		then:
		project.tasks.visualStudio.group == IDE_GROUP_NAME
		project.tasks.visualStudio.description == "Generates Visual Studio IDE configuration"
	}

	def "registers clean task"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'

		then:
		project.tasks.cleanVisualStudio.group == IDE_GROUP_NAME
		project.tasks.cleanVisualStudio.description == "Cleans Visual Studio IDE configuration"
	}

	def "registers open task only on root project"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'

		then:
		project.tasks.openVisualStudio.group == IDE_GROUP_NAME
		project.tasks.openVisualStudio.description == "Opens the Visual Studio solution"

		when:
		def subproject = ProjectBuilder.builder().withParent(project).build()
		subproject.apply plugin: 'dev.nokee.visual-studio-ide-base'

		then:
		subproject.tasks.findByName('openVisualStudio') == null
	}

	def "registers extension on project"() {
		given:
		def subproject = ProjectBuilder.builder().withParent(project).build()

		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'
		subproject.apply plugin: 'dev.nokee.visual-studio-ide-base'

		then:
		project.visualStudio != null
		project.visualStudio instanceof VisualStudioIdeWorkspaceExtension

		and:
		subproject.visualStudio != null
		subproject.visualStudio instanceof VisualStudioIdeProjectExtension
		!(subproject.visualStudio instanceof VisualStudioIdeWorkspaceExtension)
	}

	def "registers empty extensions on project"() {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'
		project.evaluate()

		then:
		project.visualStudio.workspace.displayName == 'Visual Studio solution'
		project.visualStudio.workspace.projects.get() == [] as Set
		project.visualStudio.workspace.location.get().asFile.absolutePath == separatorsToSystem("${project.projectDir}/${project.name}.sln")

		and:
		project.visualStudio.solution.displayName == 'Visual Studio solution'
		project.visualStudio.solution.projects.get() == [] as Set
		project.visualStudio.solution.location.get().asFile.absolutePath == separatorsToSystem("${project.projectDir}/${project.name}.sln")

		and:
		project.visualStudio.projects == [] as Set
	}

	def "does not register solution task on subproject"() {
		given:
		def subproject = ProjectBuilder.builder().withParent(project).build()

		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'
		subproject.apply plugin: 'dev.nokee.visual-studio-ide-base'

		then:
		project.tasks.findByName('visualStudioSolution') != null
		subproject.tasks.findByName('visualStudioSolution') == null
	}

	def "does not register projects when component plugins are applied"(componentPluginId) {
		when:
		project.apply plugin: 'dev.nokee.visual-studio-ide-base'
		project.apply plugin: componentPluginId

		then:
		project.visualStudio.projects.empty

		where:
		componentPluginId << [
			'dev.nokee.cpp-application',
			'dev.nokee.cpp-library',
			'dev.nokee.c-application',
			'dev.nokee.c-library',
			'dev.nokee.objective-c-application',
			'dev.nokee.objective-c-library',
			'dev.nokee.objective-cpp-application',
			'dev.nokee.objective-cpp-library',
			'dev.nokee.swift-application',
			'dev.nokee.swift-library',
		]
	}
}
