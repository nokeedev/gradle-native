package dev.nokee.platform.jni.fixtures

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.jni.fixtures.elements.CppGreeterJniBinding
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement
import dev.nokee.platform.jni.fixtures.elements.TestableJniLibraryElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile
import static dev.gradleplugins.test.fixtures.sources.java.JavaSourceElement.ofPackage

class KotlinJniCppGreeterLib extends JniLibraryElement {
	final NativeSourceElement nativeBindings
	final KotlinNativeGreeter jvmBindings
	final JavaSourceElement jvmImplementation
	final CppGreeter nativeImplementation
	final JavaSourceElement junitTest
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
		this.resourcePath = resourcePath
		this.projectName = projectName
		def javaPackage = ofPackage('com.example.greeter')
		String sharedLibraryBaseName = projectName
		jvmBindings = new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
		nativeBindings = new CppGreeterJniBinding(javaPackage).withJniGeneratedHeader()

		jvmImplementation = new KotlinNativeLoader(javaPackage);

		nativeImplementation = new CppGreeter()

		junitTest = new KotlinGreeterJUnitTest(javaPackage)
	}

	@Override
	TestableJniLibraryElement withJUnitTest() {
		return new TestableJniLibraryElement(this, junitTest)
	}
}

class KotlinNativeGreeter extends JavaSourceFileElement {
	private final SourceFileElement source
	private final JavaPackage javaPackage
	private final String sharedLibraryBaseName
	private final String resourcePath

	@Override
	SourceFileElement getSource() {
		return source
	}

	KotlinNativeGreeter(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath = '') {
		this.javaPackage = javaPackage
		this.sharedLibraryBaseName = sharedLibraryBaseName
		this.resourcePath = resourcePath
		source = ofFile(sourceFile("kotlin/${javaPackage.directoryLayout}", 'Greeter.kt', """
package ${javaPackage.name}

class Greeter {
	companion object {
		init {
			NativeLoader.loadLibrary(Greeter::class.java.classLoader, "${resourcePath}${sharedLibraryBaseName}")
		}
	}

	external fun sayHello(name: String?): String?
}
"""))
	}

	KotlinNativeGreeter withSharedLibraryBaseName(String sharedLibraryBaseName) {
		return new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
	}

	KotlinNativeGreeter withResourcePath(String resourcePath) {
		return new KotlinNativeGreeter(javaPackage, sharedLibraryBaseName, resourcePath)
	}
}

class KotlinNativeLoader extends JavaSourceFileElement {
	private final SourceFileElement source

	@Override
	SourceFileElement getSource() {
		return source
	}

	KotlinNativeLoader(JavaPackage javaPackage) {
		source = ofFile(sourceFile("kotlin/${javaPackage.directoryLayout}", 'NativeLoader.kt', """
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
		val osName = System.getProperty("os.name").toLowerCase()
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
"""))
	}
}

class KotlinGreeterJUnitTest extends JavaSourceFileElement {
	private final SourceFileElement source

	@Override
	SourceFileElement getSource() {
		return source
	}

	@Override
	String getSourceSetName() {
		return 'test'
	}

	KotlinGreeterJUnitTest(JavaPackage javaPackage) {
		source = ofFile(sourceFile("kotlin/${javaPackage.directoryLayout}", 'GreeterTest.java', """
package ${javaPackage.name}

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class GreeterTest {
	@Test
	fun testGreeter() {
		val greeter = Greeter()
		val greeting = greeter.sayHello("World")
		assertThat(greeting, equalTo("Bonjour, World!"))
	}

	@Test
	fun testNullGreeter() {
		val greeter = Greeter()
		val greeting = greeter.sayHello(null)
		assertThat(greeting, equalTo("name cannot be null"))
	}
}
"""))
	}
}
