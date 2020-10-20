package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.c.internal.CSourceSetImpl
import dev.nokee.platform.base.Variant
import dev.nokee.platform.c.CLibraryExtension
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent
import org.gradle.api.Project
import spock.lang.Subject

trait CLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.c-library'
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
		return CLibraryExtension
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

@Subject(CLibraryPlugin)
class CLibraryPluginTest extends AbstractPluginTest implements CLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CLibraryPlugin)
class CLibraryComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return CLibraryExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultNativeLibraryComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.c-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('c', CSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl, 'privateHeaders'), newExpectedSourceSet('public', CHeaderSetImpl, 'publicHeaders')]
	}
}

@Subject(CLibraryPlugin)
class CLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CLibraryPluginTestFixture {
}

@Subject(CLibraryPlugin)
class CLibraryTaskPluginTest extends AbstractTaskPluginTest implements CLibraryPluginTestFixture {
}

@Subject(CLibraryPlugin)
class CLibraryVariantPluginTest extends AbstractVariantPluginTest implements CLibraryPluginTestFixture {
}

@Subject(CLibraryPlugin)
class CLibraryBinaryPluginTest extends AbstractBinaryPluginTest implements CLibraryPluginTestFixture {
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
