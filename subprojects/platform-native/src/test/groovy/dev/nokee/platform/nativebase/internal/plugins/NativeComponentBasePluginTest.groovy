package dev.nokee.platform.nativebase.internal.plugins

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin
import spock.lang.Specification

class NativeComponentBasePluginTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	def "applies component model base plugin"() {
		when:
		project.apply plugin: NativeComponentBasePlugin

		then:
		project.plugins.hasPlugin(ComponentModelBasePlugin)
	}

	// TODO: Find another way to assert this
//	def "registers native application component"() {
//		when:
//		project.apply plugin: NativeComponentBasePlugin
//
//		then:
//		project.extensions.getByType(ComponentInstantiator).creatableTypes.contains(DefaultNativeApplicationComponent)
//	}

	// TODO: Find another way to assert this
//	def "registers native library component"() {
//		when:
//		project.apply plugin: NativeComponentBasePlugin
//
//		then:
//		project.extensions.getByType(ComponentInstantiator).creatableTypes.contains(DefaultNativeLibraryComponent)
//	}
}
