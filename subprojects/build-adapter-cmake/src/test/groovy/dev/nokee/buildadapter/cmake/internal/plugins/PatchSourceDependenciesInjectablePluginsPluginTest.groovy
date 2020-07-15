package dev.nokee.buildadapter.cmake.internal.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PatchSourceDependenciesInjectablePluginsPluginTest extends Specification {
	def "it works"() {
		def project = ProjectBuilder.builder().build()
		project.apply plugin: 'dev.nokee.injectable-source-dependency-plugins'

		expect:
		project.repositories.size() == 3

		project.configurations.getByName("implementation").dependencies.any { it.group == 'dev.nokee' && it.name == 'nokee-gradle-plugins' }

		project.pluginManager.hasPlugin('java-gradle-plugin')

		project.gradlePlugin.plugins.size() == 1
	}
}
