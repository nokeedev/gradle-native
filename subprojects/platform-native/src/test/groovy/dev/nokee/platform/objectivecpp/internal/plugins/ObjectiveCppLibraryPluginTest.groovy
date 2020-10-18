package dev.nokee.platform.objectivecpp.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCppLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-cpp-library'
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
		return ObjectiveCppLibraryExtension
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

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppLibraryPluginTest extends AbstractPluginTest implements ObjectiveCppLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

class ObjectiveCppLibraryComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return ObjectiveCppLibraryExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultNativeLibraryComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.objective-cpp-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('objectiveCpp', ObjectiveCppSourceSetImpl).addConventionDirectory('src/main/objcpp'), newExpectedSourceSet('headers', CppHeaderSetImpl, 'privateHeaders'), newExpectedSourceSet('public', CppHeaderSetImpl, 'publicHeaders')]
	}
}

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements ObjectiveCppLibraryPluginTestFixture {
}

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppLibraryTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCppLibraryPluginTestFixture {
}

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppLibraryVariantPluginTest extends AbstractVariantPluginTest implements ObjectiveCppLibraryPluginTestFixture {
}

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppLibraryBinaryPluginTest extends AbstractBinaryPluginTest implements ObjectiveCppLibraryPluginTestFixture {
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
