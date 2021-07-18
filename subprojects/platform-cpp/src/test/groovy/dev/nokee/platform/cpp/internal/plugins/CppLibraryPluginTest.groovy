package dev.nokee.platform.cpp.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.platform.base.Variant
import dev.nokee.platform.cpp.CppLibrary
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import org.gradle.api.Project
import spock.lang.Subject

trait CppLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.cpp-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	String getExtensionNameUnderTest() {
		return 'library'
	}

	Class getExtensionType() {
		return CppLibrary
	}

	Class getVariantType() {
		return NativeLibrary
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'sharedLibrary']
	}

	void configureMultipleVariants() {
		extensionUnderTest.targetMachines = [extensionUnderTest.machines.macOS, extensionUnderTest.machines.windows, extensionUnderTest.machines.linux]
	}
}

@Subject(CppLibraryPlugin)
class CppLibraryPluginTest extends AbstractPluginTest implements CppLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CppLibraryPlugin)
class CppLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CppLibraryPluginTestFixture {
}

@Subject(CppLibraryPlugin)
class CppLibraryTaskPluginTest extends AbstractTaskPluginTest implements CppLibraryPluginTestFixture {
}

@Subject(CppLibraryPlugin)
class CppLibraryVariantPluginTest extends AbstractVariantPluginTest implements CppLibraryPluginTestFixture {
}

@Subject(CppLibraryPlugin)
class CppLibraryBinaryPluginTest extends AbstractBinaryPluginTest implements CppLibraryPluginTestFixture {
	@Override
	boolean hasExpectedBinaries(Variant variant) {
		variant.binaries.get().with { binaries ->
			assert binaries.size() == 1
			assert binaries.any { it instanceof SharedLibraryBinary }
		}
		return true
	}

	@Override
	boolean hasExpectedBinaries(Object extension) {
		if (extension.targetMachines.get().size() == 1) {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 1
				assert binaries.any { it instanceof SharedLibraryBinary }
			}
		} else {
			extension.binaries.get().with { binaries ->
				assert binaries.size() == 3
				assert binaries.count { it instanceof SharedLibraryBinary } == 3
			}
		}
		return true
	}
}
