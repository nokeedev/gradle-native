package dev.nokee.platform.jni

import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCGreeterLib

import static org.hamcrest.CoreMatchers.containsString

class JavaCJniLibraryFunctionalTest extends AbstractJavaJniLibraryFunctionalTest implements CTaskNames {
	@Override
	protected String getDevelopmentBinaryNativeCompileTask() {
		return tasks.compile
	}

	@Override
	protected void makeSingleProject() {
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.c-language'
            }
        '''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	JavaJniCGreeterLib getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter')
	}

	def "build fails when C compilation fails"() {
		given:
		makeSingleProject()

		and:
		file("src/main/c/broken.c") << "broken!"

		expect:
		fails "assemble"
		failure.assertHasDescription("Execution failed for task '$developmentBinaryNativeCompileTask'.")
		failure.assertHasCause("A build operation failed.")
		failure.assertThatCause(containsString("C compiler failed while compiling broken.c"))
	}
}
