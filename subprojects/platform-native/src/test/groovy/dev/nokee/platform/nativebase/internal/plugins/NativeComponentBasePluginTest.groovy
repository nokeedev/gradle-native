package dev.nokee.platform.nativebase.internal.plugins

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.platform.base.internal.components.ComponentInstantiator
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent
import spock.lang.Specification

class NativeComponentBasePluginTest extends Specification {
	def project = TestUtils.rootProject()

	def "applies component model base plugin"() {
		when:
		project.apply plugin: NativeComponentBasePlugin

		then:
		project.plugins.hasPlugin(ComponentModelBasePlugin)
	}

	def "registers native application component"() {
		when:
		project.apply plugin: NativeComponentBasePlugin

		then:
		project.extensions.getByType(ComponentInstantiator).creatableTypes.contains(DefaultNativeApplicationComponent)
	}

	def "registers native library component"() {
		when:
		project.apply plugin: NativeComponentBasePlugin

		then:
		project.extensions.getByType(ComponentInstantiator).creatableTypes.contains(DefaultNativeLibraryComponent)
	}
}
