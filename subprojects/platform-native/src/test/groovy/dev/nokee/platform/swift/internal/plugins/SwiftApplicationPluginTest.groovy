package dev.nokee.platform.swift.internal.plugins

import dev.nokee.fixtures.*
import dev.nokee.language.swift.internal.SwiftSourceSetImpl
import dev.nokee.platform.base.Variant
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeApplication
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent
import dev.nokee.platform.swift.SwiftApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-application'
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
		return SwiftApplicationExtension
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

@Subject(SwiftApplicationPlugin)
class SwiftApplicationPluginTest extends AbstractPluginTest implements SwiftApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return SwiftApplicationExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultNativeApplicationComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.swift-application'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('swift', SwiftSourceSetImpl)]
	}
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements SwiftApplicationPluginTestFixture {
}

@Subject(SwiftLibraryPlugin)
class SwiftApplicationTaskPluginTest extends AbstractTaskPluginTest implements SwiftApplicationPluginTestFixture {
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationVariantPluginTest extends AbstractVariantPluginTest implements SwiftApplicationPluginTestFixture {
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationBinaryPluginTest extends AbstractBinaryPluginTest implements SwiftApplicationPluginTestFixture {
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
