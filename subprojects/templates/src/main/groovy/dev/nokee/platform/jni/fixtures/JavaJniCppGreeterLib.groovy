package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.java.JavaPackage
import dev.nokee.platform.jni.fixtures.elements.*

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage
import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

class JavaJniCppGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
	final CppGreeterJniBinding nativeBindings
	final JavaNativeGreeter jvmBindings
	final SourceElement jvmImplementation
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
		this(ofPackage('com.example.greeter'), projectName, resourcePath)
		this.resourcePath = resourcePath
		this.projectName = projectName
	}

	private JavaJniCppGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
		this(new JavaNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath), new CppGreeterJniBinding(javaPackage), new JavaNativeLoader(javaPackage), new CppGreeter())
	}

	private JavaJniCppGreeterLib(JavaNativeGreeter jvmBindings, CppGreeterJniBinding nativeBindings, JavaNativeLoader jvmImplementation, CppGreeter nativeImplementation) {
		super(ofElements(jvmBindings, nativeBindings, jvmImplementation), nativeImplementation)
		this.jvmBindings = jvmBindings
		this.nativeBindings = nativeBindings
		this.jvmImplementation = jvmImplementation
		this.nativeImplementation = nativeImplementation
	}

	JavaJniCppGreeterLib withProjectName(String projectName) {
		return new JavaJniCppGreeterLib(projectName, resourcePath)
	}

	JavaJniCppGreeterLib withResourcePath(String resourcePath) {
		assert resourcePath.endsWith('/')
		return new JavaJniCppGreeterLib(projectName, resourcePath);
	}

	JniLibraryElement withoutNativeImplementation() {
		return new SimpleJniLibraryElement(ofElements(jvmBindings, jvmImplementation), nativeBindings)
	}

	JniLibraryElement withoutJvmImplementation() {
		return new SimpleJniLibraryElement(jvmBindings, ofNativeElements(nativeBindings, nativeImplementation))
	}

	JniLibraryElement withImplementationAsSubprojects() {
		return new SimpleJniLibraryElement(null, null) {
			@Override
			SourceElement getJvmSources() {
				throw new UnsupportedOperationException()
			}

			@Override
			NativeSourceElement getNativeSources() {
				throw new UnsupportedOperationException()
			}

			@Override
			void writeToProject(File projectDir) {
				jvmBindings.withSharedLibraryBaseName('cpp-jni-greeter').writeToProject(file(projectDir, 'java-jni-greeter'))
				nativeBindings.withJniGeneratedHeader().writeToProject(file(projectDir, 'cpp-jni-greeter'))
				jvmImplementation.writeToProject(file(projectDir, 'java-loader'))
				nativeImplementation.asLib().writeToProject(file(projectDir, 'cpp-greeter'))
			}
		}
	}

	JniLibraryElement withOptionalFeature() {
		return new SimpleJniLibraryElement(jvmSources, ofNativeElements(nativeBindings, nativeImplementation.withOptionalFeature()))
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, nativeImplementation.asLib()))
	}
}

