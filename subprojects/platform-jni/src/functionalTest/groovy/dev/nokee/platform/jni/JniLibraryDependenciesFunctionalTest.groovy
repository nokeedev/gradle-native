package dev.nokee.platform.jni


import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.DefaultJavaProjectTasks
import dev.nokee.language.DefaultNativeProjectTasks
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.JavaJniTaskNames

abstract class AbstractJniLibraryProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest {
	@Override
	protected String getComponentUnderTestDsl() {
		return 'library'
	}
}

class JniLibraryNativeProjectDependenciesFunctionalTest extends AbstractJniLibraryProjectDependenciesFunctionalTest implements CppTaskNames, JavaJniTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild() << '''
			rootProject.name = 'jni-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		file(libraryProjectName, buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''

		def fixture = new JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(testDirectory.file(libraryProjectName))
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'nativeImplementation'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return new DefaultNativeProjectTasks(":${libraryProjectName}", 'Cpp').allToLink
	}
}

class JniLibraryJvmProjectDependenciesFunctionalTest extends AbstractJniLibraryProjectDependenciesFunctionalTest implements CppTaskNames, JavaJniTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild() << '''
			rootProject.name = 'jni-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		file(libraryProjectName, buildFileName) << '''
			plugins {
				id 'java'
			}
		'''

		def fixture = new JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutJvmImplementation().writeToProject(testDirectory)
		fixture.jvmImplementation.writeToProject(testDirectory.file(libraryProjectName))
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'jvmImplementation'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return new DefaultJavaProjectTasks(":${libraryProjectName}").allToJar
	}

	@Override
	protected boolean canDefineDependencyOnVariants() {
		return false
	}
}

abstract class AbstractJniLibraryIncludedDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected String getComponentUnderTestDsl() {
		return 'library'
	}
}

class JniLibraryNativeIncludedDependenciesFunctionalTest extends AbstractJniLibraryIncludedDependenciesFunctionalTest implements CppTaskNames, JavaJniTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild() << '''
			rootProject.name = 'jni-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		file(libraryProjectName, settingsFileName) << """
			rootProject.name = '${libraryProjectName}'
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.cpp-library'
			}

			${configureLibraryProject()}
		"""

		def fixture = new JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutNativeImplementation().writeToProject(testDirectory)
		fixture.nativeImplementation.asLib().writeToProject(testDirectory.file(libraryProjectName))
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'nativeImplementation'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return new DefaultNativeProjectTasks(":${libraryProjectName}", 'Cpp').allToLink
	}
}

class JniLibraryJvmIncludedDependenciesFunctionalTest extends AbstractJniLibraryIncludedDependenciesFunctionalTest implements CppTaskNames, JavaJniTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild() << '''
			rootProject.name = 'jni-greeter'
		'''
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}
		'''
		file(libraryProjectName, settingsFileName) << """
			rootProject.name = '${libraryProjectName}'
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'java'
			}

			${configureLibraryProject()}
		"""

		def fixture = new JavaJniCppGreeterLib('jni-greeter')
		fixture.withoutJvmImplementation().writeToProject(testDirectory)
		fixture.jvmImplementation.writeToProject(testDirectory.file(libraryProjectName))
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'jvmImplementation'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return new DefaultJavaProjectTasks(":${libraryProjectName}").allToJar
	}

	@Override
	protected boolean canDefineDependencyOnVariants() {
		return false
	}
}
