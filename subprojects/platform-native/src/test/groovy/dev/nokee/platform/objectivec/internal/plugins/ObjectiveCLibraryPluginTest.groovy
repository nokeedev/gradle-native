package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-library'
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
		return ObjectiveCLibraryExtension
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

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryPluginTest extends AbstractPluginTest implements ObjectiveCLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

class ObjectiveCLibraryComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return ObjectiveCLibraryExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultNativeLibraryComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.objective-c-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('objectiveC', ObjectiveCSourceSetImpl).addConventionDirectory('src/main/objc'), newExpectedSourceSet('headers', CHeaderSetImpl, 'privateHeaders'), newExpectedSourceSet('public', CHeaderSetImpl, 'publicHeaders')]
	}
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements ObjectiveCLibraryPluginTestFixture {
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCLibraryPluginTestFixture {
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryVariantPluginTest extends AbstractVariantPluginTest implements ObjectiveCLibraryPluginTestFixture {
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryBinaryPluginTest extends AbstractBinaryPluginTest implements ObjectiveCLibraryPluginTestFixture {
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
