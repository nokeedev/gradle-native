package dev.nokee.platform.jni.fixtures.elements

import dev.gradleplugins.fixtures.sources.SourceFile
import dev.gradleplugins.fixtures.sources.SourceFileElement
import dev.gradleplugins.fixtures.sources.java.JavaPackage

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage

class JavaGreeter extends SourceFileElement {
	private final SourceFile source

	@Override
	SourceFile getSourceFile() {
		return source
	}

	JavaGreeter(JavaPackage javaPackage = ofPackage('com.example.greeter')) {
		source = sourceFile("java/${javaPackage.directoryLayout}", 'Greeter.java', """
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
""")
	}
}
