package dev.nokee.platform.objectivecpp.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeApplication
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent
import dev.nokee.platform.objectivecpp.ObjectiveCppApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCppApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-cpp-application'
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
		return ObjectiveCppApplicationExtension
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

@Subject(ObjectiveCppApplicationPlugin)
class ObjectiveCppApplicationPluginTest extends AbstractPluginTest implements ObjectiveCppApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(ObjectiveCppApplicationPlugin)
class ObjectiveCppApplicationComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return ObjectiveCppApplicationExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultNativeApplicationComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.objective-cpp-application'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('objectiveCpp', ObjectiveCppSourceSetImpl).addConventionDirectory('src/main/objcpp'), newExpectedSourceSet('headers', CppHeaderSetImpl, 'privateHeaders')]
	}
}

@Subject(ObjectiveCppApplicationPlugin)
class ObjectiveCppApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements ObjectiveCppApplicationPluginTestFixture {
}

@Subject(ObjectiveCppApplicationPlugin)
class ObjectiveCppApplicationTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCppApplicationPluginTestFixture {
}

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppApplicationVariantPluginTest extends AbstractVariantPluginTest implements ObjectiveCppApplicationPluginTestFixture {
}

@Subject(ObjectiveCppApplicationPlugin)
class ObjectiveCppApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements ObjectiveCppApplicationPluginTestFixture {
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
