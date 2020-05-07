package dev.nokee.platform.jni

abstract class AbstractJavaJniLibraryFunctionalTest extends AbstractJniLibraryFunctionalTest {
	def "generate JNI headers when compiling Java source code"() {
		makeSingleProject()
		componentUnderTest.writeToProject(testDirectory)

		when:
		file('build/generated/jni-headers').assertDoesNotExist()
		succeeds('compileJava')

		then:
		file('build/generated/jni-headers').assertHasDescendants('com_example_greeter_Greeter.h')
	}

	def "build fails when Java compilation fails"() {
		given:
		makeSingleProject()

		and:
		file("src/main/java/broken.java") << "broken!"

		expect:
		fails "assemble"
		failure.assertHasDescription("Execution failed for task ':compileJava'.")
		failure.assertHasCause("Compilation failed; see the compiler error output for details.")
	}

	protected List<String> getExpectedClasses() {
		return ['com/example/greeter/Greeter.class', 'com/example/greeter/NativeLoader.class']
	}

	@Override
	protected String getJvmPluginId() {
		return 'java'
	}
}
