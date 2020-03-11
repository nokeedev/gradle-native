package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.MixedLanguageTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class JniLibraryTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements MixedLanguageTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter')
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected List<String> getTasksToAssembleDevelopmentBinary() {
		return taskNames.java.tasks.allToAssemble + taskNames.cpp.tasks.allToAssemble
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinaryWithArchitecture(String architecture) {
		return null
	}

	@Override
	protected String getComponentName() {
		return 'main'
	}

	@Override
	protected String getProjectName() {
		return 'jni-greeter'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class', sharedLibraryName('jni-greeter'))
		} else {
			jar("build/libs/jni-greeter.jar").hasDescendants('com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class')
			jar("build/libs/jni-greeter-${variant}.jar").hasDescendants(sharedLibraryName('jni-greeter'))
		}
	}

	protected JarTestFixture jar(String path) {
		return new JarTestFixture(file(path))
	}
}
