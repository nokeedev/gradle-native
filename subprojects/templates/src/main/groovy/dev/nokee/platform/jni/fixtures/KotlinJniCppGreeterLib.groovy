package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.fixtures.sources.NativeSourceElement
import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement
import dev.gradleplugins.fixtures.sources.java.JavaPackage
import dev.nokee.platform.jni.fixtures.elements.*

import static dev.gradleplugins.fixtures.sources.NativeSourceElement.ofNativeElements
import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage

class KotlinJniCppGreeterLib extends GreeterImplementationAwareSourceElement<NativeSourceElement> implements JniLibraryElement {
	final NativeSourceElement nativeBindings
	final KotlinNativeGreeter jvmBindings
	final SourceElement jvmImplementation
	final CppGreeter nativeImplementation
	final SourceElement junitTest
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

	KotlinJniCppGreeterLib(String projectName, String resourcePath = '') {
		this(ofPackage('com.example.greeter'), projectName, resourcePath)
		this.resourcePath = resourcePath
		this.projectName = projectName
	}

	private KotlinJniCppGreeterLib(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
		this(new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath), new CppGreeterJniBinding(javaPackage).withJniGeneratedHeader(), new KotlinNativeLoader(javaPackage), new CppGreeter(), new KotlinGreeterJUnitTest(javaPackage))
	}

	private KotlinJniCppGreeterLib(KotlinNativeGreeter jvmBindings, NativeSourceElement nativeBindings, KotlinNativeLoader jvmImplementation, CppGreeter nativeImplementation, KotlinGreeterJUnitTest junitTest) {
		super(ofElements(jvmBindings, nativeBindings, jvmImplementation), nativeImplementation)
		this.jvmBindings = jvmBindings
		this.nativeBindings = nativeBindings
		this.jvmImplementation = jvmImplementation
		this.nativeImplementation = nativeImplementation
		this.junitTest = junitTest
	}

	@Override
	TestableJniLibraryElement withJUnitTest() {
		return new TestableJniLibraryElement(this, junitTest)
	}

	@Override
	GreeterImplementationAwareSourceElement<NativeSourceElement> withImplementationAsSubproject(String subprojectPath) {
		return ofImplementationAsSubproject(elementUsingGreeter, asSubproject(subprojectPath, nativeImplementation.asLib()))
	}

	private static class KotlinNativeGreeter extends SourceFileElement {
		private final SourceFile source
		private final JavaPackage javaPackage
		private final String sharedLibraryBaseName
		private final String resourcePath

		@Override
		SourceFile getSourceFile() {
			return source
		}

		KotlinNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath = '') {
			this.javaPackage = javaPackage
			this.sharedLibraryBaseName = sharedLibraryBaseName
			this.resourcePath = resourcePath
			source = sourceFile("kotlin/${javaPackage.directoryLayout}", 'Greeter.kt', """
package ${javaPackage.name}

class Greeter {
	companion object {
		init {
			NativeLoader.loadLibrary(Greeter::class.java.classLoader, "${resourcePath}${sharedLibraryBaseName}")
		}
	}

	external fun sayHello(name: String?): String?
}
""")
		}

		KotlinNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
			return new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
		}

		KotlinNativeGreeter withResourcePath(String resourcePath) {
			return new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
		}
	}

	private static class KotlinNativeLoader extends SourceFileElement {
		private final SourceFile source

		@Override
		SourceFile getSourceFile() {
			return source
		}

		KotlinNativeLoader(JavaPackage javaPackage) {
			source = sourceFile("kotlin/${javaPackage.directoryLayout}", 'NativeLoader.kt', """
package ${javaPackage.name}

import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files

object NativeLoader {
	fun loadLibrary(classLoader: ClassLoader, libName: String) {
		try {
			System.loadLibrary(libName)
		} catch (ex: UnsatisfiedLinkError) {
			val url = classLoader.getResource(libFilename(libName))
			try {
				val file = Files.createTempFile("jni", libFilename(nameOnly(libName))).toFile()
				file.deleteOnExit()
				file.delete()
				url.openStream().use { `in` -> Files.copy(`in`, file.toPath()) }
				System.load(file.canonicalPath)
			} catch (e: IOException) {
				throw UncheckedIOException(e)
			}
		}
	}

	private fun libFilename(libName: String): String {
		val osName = System.getProperty("os.name").lowercase()
		if (osName.indexOf("win") >= 0) {
			return "\$libName.dll"
		} else if (osName.indexOf("mac") >= 0) {
			return decorateLibraryName(libName, ".dylib")
		}
		return decorateLibraryName(libName, ".so")
	}

	private fun nameOnly(libName: String): String {
		val pos = libName.lastIndexOf('/')
		return if (pos >= 0) {
			libName.substring(pos + 1)
		} else libName
	}

	private fun decorateLibraryName(libraryName: String, suffix: String): String {
		if (libraryName.endsWith(suffix)) {
			return libraryName
		}
		val pos = libraryName.lastIndexOf('/')
		return if (pos >= 0) {
			libraryName.substring(0, pos + 1) + "lib" + libraryName.substring(pos + 1) + suffix
		} else {
			"lib\$libraryName\$suffix"
		}
	}
}
""")
		}
	}

	private static class KotlinGreeterJUnitTest extends SourceFileElement {
		private final SourceFile source

		@Override
		SourceFile getSourceFile() {
			return source
		}

		@Override
		String getSourceSetName() {
			return 'test'
		}

		KotlinGreeterJUnitTest(JavaPackage javaPackage) {
			source = sourceFile("kotlin/${javaPackage.directoryLayout}", 'GreeterTest.kt', """
package ${javaPackage.name}

import kotlin.test.assertEquals
import kotlin.test.Test

internal class GreeterTest {
	@Test
	fun testGreeter() {
		val greeter = Greeter()
		val greeting = greeter.sayHello("World")
		assertEquals("Bonjour, World!", greeting)
	}

	@Test
	fun testNullGreeter() {
		val greeter = Greeter()
		val greeting = greeter.sayHello(null)
		assertEquals("name cannot be null", greeting)
	}
}
""")
		}
	}
}
