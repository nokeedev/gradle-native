package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCppGreeterLib
import spock.lang.Requires
import spock.util.environment.OperatingSystem

import static org.hamcrest.CoreMatchers.containsString

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class JavaObjectiveCppJniLibraryFunctionalTest extends AbstractJavaJniLibraryFunctionalTest implements ObjectiveCppTaskNames {
	def "build fails when Objective-C++ compilation fails"() {
		given:
		makeSingleProject()

		and:
		file("src/main/objcpp/broken.mm") << "broken!"

		expect:
		fails "assemble"
		failure.assertHasDescription("Execution failed for task '${tasks.compile}'.")
		failure.assertHasCause("A build operation failed.")
		failure.assertThatCause(containsString("Objective-C++ compiler failed while compiling broken.mm"))
	}


//	def "can compile and link against a library"() {
// TODO: JNI Plugin should allow to work alone by embedding the runtime native dependencies into a JAR. It should also create an empty JAR when no language (not even Java). This Jar should be exposed via API outgoing dependencies.

	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-cpp-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	JavaJniObjectiveCppGreeterLib getComponentUnderTest() {
		return new JavaJniObjectiveCppGreeterLib('jni-greeter')
	}

	@Override
	protected String getNativePluginId() {
		return 'dev.nokee.objective-cpp-language'
	}
}
