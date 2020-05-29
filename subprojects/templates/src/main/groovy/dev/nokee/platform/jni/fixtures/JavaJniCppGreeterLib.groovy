package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.jni.fixtures.elements.CppGreeterJniBinding
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class JavaJniCppGreeterLib extends JniLibraryElement {
	final CppGreeterJniBinding nativeBindings
	final JavaNativeGreeter jvmBindings
	final JavaSourceElement jvmImplementation
	final CppGreeter nativeImplementation
	private final String projectName
	private final String resourcePath

	@Override
	SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation)
	}

	@Override
	NativeSourceElement getNativeSources() {
		return ofNativeElements(nativeBindings, nativeImplementation);
	}

	JavaJniCppGreeterLib(String projectName, String resourcePath = '') {
		this.resourcePath = resourcePath
		this.projectName = projectName
		def javaPackage = ofPackage('com.example.greeter')
		String sharedLibraryBaseName = projectName
		jvmBindings = new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
		nativeBindings = new CppGreeterJniBinding(javaPackage)

		jvmImplementation = new JavaNativeLoader(javaPackage);

		nativeImplementation = new CppGreeter()
	}

	JavaJniCppGreeterLib withProjectName(String projectName) {
		return new JavaJniCppGreeterLib(projectName, resourcePath)
	}

	JavaJniCppGreeterLib withResourcePath(String resourcePath) {
		assert resourcePath.endsWith('/')
		return new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	JniLibraryElement withoutNativeImplementation() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return ofElements(JavaJniCppGreeterLib.this.jvmBindings, JavaJniCppGreeterLib.this.jvmImplementation)
			}

			@Override
			NativeSourceElement getNativeSources() {
				return nativeBindings
			}
		}
	}

	JniLibraryElement withImplementationAsSubprojects() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				throw new UnsupportedOperationException()
			}

			@Override
			NativeSourceElement getNativeSources() {
				throw new UnsupportedOperationException()
			}

			@Override
			void writeToProject(TestFile projectDir) {
				jvmBindings.withSharedLibraryBaseName('cpp-jni-greeter').writeToProject(projectDir.file('java-jni-greeter'))
				nativeBindings.withJniGeneratedHeader().writeToProject(projectDir.file('cpp-jni-greeter'))
				jvmImplementation.writeToProject(projectDir.file('java-loader'))
				nativeImplementation.asLib().writeToProject(projectDir.file('cpp-greeter'))
			}
		}
	}

	JniLibraryElement withOptionalFeature() {
		return new JniLibraryElement() {
			@Override
			SourceElement getJvmSources() {
				return JavaJniCppGreeterLib.this.jvmSources
			}

			@Override
			NativeSourceElement getNativeSources() {
				return ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature())
			}
		}
	}
}

