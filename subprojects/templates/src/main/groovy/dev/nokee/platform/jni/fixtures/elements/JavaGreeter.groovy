package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.test.fixtures.sources.SourceFileElement
import dev.gradleplugins.test.fixtures.sources.java.JavaPackage
import dev.gradleplugins.test.fixtures.sources.java.JavaSourceFileElement

import static dev.gradleplugins.test.fixtures.sources.SourceFileElement.ofFile

class JavaGreeter extends JavaSourceFileElement {
	private final SourceFileElement source

	@Override
	SourceFileElement getSource() {
		return source
	}

	JavaGreeter(JavaPackage javaPackage = ofPackage('com.example.greeter')) {
		source = ofFile(sourceFile("java/${javaPackage.directoryLayout}", 'Greeter.java', """
package ${javaPackage.name};

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;

public class Greeter {
	public String sayHello(String name) {
		return "Bonjour, " + name + "!";
	}
}
"""))
	}
}
