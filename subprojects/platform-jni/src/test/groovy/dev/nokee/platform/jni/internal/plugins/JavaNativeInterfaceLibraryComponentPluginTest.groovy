package dev.nokee.platform.jni.internal.plugins

import dev.nokee.fixtures.AbstractComponentPluginTest
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.c.internal.CSourceSetImpl
import dev.nokee.language.cpp.internal.CppHeaderSetImpl
import dev.nokee.language.cpp.internal.CppSourceSetImpl
import dev.nokee.language.jvm.internal.GroovySourceSetImpl
import dev.nokee.language.jvm.internal.JavaSourceSetImpl
import dev.nokee.language.jvm.internal.KotlinSourceSetImpl
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetImpl
import dev.nokee.platform.jni.JniLibraryExtension
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal
import org.gradle.api.Project
import spock.lang.Subject

@Subject(JniLibraryPlugin)
abstract class AbstractJavaNativeLibraryComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return JniLibraryExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return JniLibraryComponentInternal
	}
}

//region JVM language
class JavaNativeInterfaceLibraryComponentPlugin_JavaLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'java'
		project.apply plugin: 'dev.nokee.jni-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_GroovyLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'groovy'
		project.apply plugin: 'dev.nokee.jni-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('groovy', GroovySourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_KotlinLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		project.apply plugin: 'dev.nokee.jni-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('kotlin', KotlinSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl)]
	}
}
//endregion

//region no language
class JavaNativeInterfaceLibraryComponentPluginTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.jni-library'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return []
	}
}
//endregion

//region no JVM language
class JavaNativeInterfaceLibraryComponentPlugin_CLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('c', CSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_CppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('cpp', CppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_ObjectiveCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('objectiveC', ObjectiveCSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_ObjectiveCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('objectiveCpp', ObjectiveCppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}
//endregion

//region Java language
class JavaNativeInterfaceLibraryComponentPlugin_JavaCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'java'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('c', CSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_JavaCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'java'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('cpp', CppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_JavaObjectiveCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'java'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('objectiveC', ObjectiveCSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_JavaObjectiveCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'java'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('objectiveCpp', ObjectiveCppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}
//endregion

//region Groovy language
class JavaNativeInterfaceLibraryComponentPlugin_GroovyCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'groovy'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('groovy', GroovySourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('c', CSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_GroovyCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'groovy'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('groovy', GroovySourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('cpp', CppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_GroovyObjectiveCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'groovy'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('groovy', GroovySourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('objectiveC', ObjectiveCSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_GroovyObjectiveCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'groovy'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('groovy', GroovySourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('objectiveCpp', ObjectiveCppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}
//endregion

//region Kotlin language
class JavaNativeInterfaceLibraryComponentPlugin_KotlinCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('kotlin', KotlinSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('c', CSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_KotlinCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('kotlin', KotlinSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('cpp', CppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_KotlinObjectiveCLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-c-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('kotlin', KotlinSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('objectiveC', ObjectiveCSourceSetImpl), newExpectedSourceSet('headers', CHeaderSetImpl)]
	}
}

class JavaNativeInterfaceLibraryComponentPlugin_KotlinObjectiveCppLanguageTest extends AbstractJavaNativeLibraryComponentPluginTest {
	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'org.jetbrains.kotlin.jvm'
		project.apply plugin: 'dev.nokee.jni-library'
		project.apply plugin: 'dev.nokee.objective-cpp-language'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('java', JavaSourceSetImpl), newExpectedSourceSet('kotlin', KotlinSourceSetImpl), newExpectedSourceSet('jni', CHeaderSetImpl), newExpectedSourceSet('objectiveCpp', ObjectiveCppSourceSetImpl), newExpectedSourceSet('headers', CppHeaderSetImpl)]
	}
}
//endregion
