package dev.nokee.platform.jni.fixtures;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.SourceFileElement;
import dev.gradleplugins.fixtures.sources.java.JavaPackage;
import dev.nokee.platform.jni.fixtures.elements.JavaGreeterJUnitTest;
import dev.nokee.platform.jni.fixtures.elements.JavaNativeLoader;
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.fixtures.sources.java.JavaPackage.ofPackage;

public final class JavaJniObjectiveCNSSavePanelLib extends SourceElement implements JniLibraryElement {
	private final ObjectiveCNSSavePanelJniBinding nativeBindings;
	private final SourceElement jvmBindings;
	private final SourceElement jvmImplementation;

	@Override
	public SourceElement getJvmSources() {
		return ofElements(jvmBindings, jvmImplementation);
	}

	@Override
	public SourceElement getNativeSources() {
		return nativeBindings;
	}

	@Override
	public SourceElement withJUnitTest() {
		return ofElements(this, new JavaGreeterJUnitTest());
	}

	public JavaJniObjectiveCNSSavePanelLib(String projectName) {
		JavaPackage javaPackage = ofPackage("com.example.cocoa");
		String sharedLibraryBaseName = projectName;
		jvmBindings = new JavaNativeNSSavePanel(javaPackage, sharedLibraryBaseName);
		nativeBindings = new ObjectiveCNSSavePanelJniBinding(javaPackage);

		jvmImplementation = new JavaNativeLoader(javaPackage);
	}

	@Override
	public List<SourceFile> getFiles() {
		return Stream.concat(getJvmSources().getFiles().stream(), getNativeSources().getFiles().stream()).collect(Collectors.toList());
	}

	private static class JavaNativeNSSavePanel extends SourceFileElement {
		private final SourceFile source;
		private final JavaPackage javaPackage;
		private final String sharedLibraryBaseName;
		private final String resourcePath;

		@Override
		public SourceFile getSourceFile() {
			return source;
		}

		public JavaNativeNSSavePanel(JavaPackage javaPackage, String sharedLibraryBaseName) {
			this(javaPackage, sharedLibraryBaseName, "");
		}

		public JavaNativeNSSavePanel(JavaPackage javaPackage, String sharedLibraryBaseName, String resourcePath) {
			this.javaPackage = javaPackage;
			this.sharedLibraryBaseName = sharedLibraryBaseName;
			this.resourcePath = resourcePath;
			source = sourceFile("java/" + javaPackage.getDirectoryLayout(), "NSSavePanel.java", fromResource("jni-objc-cocoa/NSSavePanel.java").replace("package " + ofPackage("com.example.cocoa").getName(), "package " + javaPackage.getName()).replace("${resourcePath}${sharedLibraryBaseName}", resourcePath + sharedLibraryBaseName));
		}

		public JavaNativeNSSavePanel withSharedLibraryBaseName(String sharedLibraryBaseName) {
			return new JavaNativeNSSavePanel(javaPackage, sharedLibraryBaseName, resourcePath);
		}

		public JavaNativeNSSavePanel withResourcePath(String resourcePath) {
			return new JavaNativeNSSavePanel(javaPackage, sharedLibraryBaseName, resourcePath);
		}
	}

	private static class ObjectiveCNSSavePanelJniBinding extends SourceFileElement {
		private final SourceFile source;

		public ObjectiveCNSSavePanelJniBinding(JavaPackage javaPackage) {
			source = sourceFile("objc", "ns_save_panel.m", fromResource("jni-objc-cocoa/ns_save_panel.m").replace(ofPackage("com.example.cocoa").jniHeader("NSSavePanel"), javaPackage.jniHeader("NSSavePanel")).replace(ofPackage("com.example.cocoa").jniMethodName("NSSavePanel", "saveDialog"), javaPackage.jniMethodName("NSSavePanel", "saveDialog")));
		}

		@Override
		public SourceFile getSourceFile() {
			return source;
		}
	}
}
