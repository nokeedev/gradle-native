package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeApplication
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	String getExtensionNameUnderTest() {
		return 'application'
	}

	Class getExtensionType() {
		return ObjectiveCApplicationExtension
	}

	Class getVariantType() {
		return NativeApplication
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'executable']
	}

	void configureMultipleVariants() {
		extensionUnderTest.targetMachines = [extensionUnderTest.machines.macOS, extensionUnderTest.machines.windows, extensionUnderTest.machines.linux]
	}
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationPluginTest extends AbstractPluginTest implements ObjectiveCApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements ObjectiveCApplicationPluginTestFixture {
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCApplicationPluginTestFixture {
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationVariantPluginTest extends AbstractVariantPluginTest implements ObjectiveCApplicationPluginTestFixture {
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements ObjectiveCApplicationPluginTestFixture {
	@Override
	boolean hasExpectedBinaries(Variant variant) {
		variant.binaries.get().with { binaries ->
			assert binaries.size() == 1
			assert binaries.any { it instanceof ExecutableBinary }
		}
		return true
	}

	@Override
	boolean hasExpectedBinaries(Object extension) {
		if (extension.targetMachines.get().size() == 1) {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 1
				assert binaries.any { it instanceof ExecutableBinary }
			}
		} else {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 3
				assert binaries.count { it instanceof ExecutableBinary } == 3
			}
		}
		return true
	}
}
