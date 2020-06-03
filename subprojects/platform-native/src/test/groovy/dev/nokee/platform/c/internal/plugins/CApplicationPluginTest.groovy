package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.AbstractBinaryPluginTest
import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.fixtures.AbstractVariantPluginTest
import dev.nokee.platform.base.Variant
import dev.nokee.platform.c.CApplicationExtension
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeApplication
import org.gradle.api.Project
import org.gradle.nativeplatform.NativeExecutable
import spock.lang.Subject

trait CApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.c-application'
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
		return CApplicationExtension
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

@Subject(CApplicationPlugin)
class CApplicationPluginTest extends AbstractPluginTest implements CApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CApplicationPlugin)
class CApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationTaskPluginTest extends AbstractTaskPluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationVariantPluginTest extends AbstractVariantPluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements CApplicationPluginTestFixture {
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
