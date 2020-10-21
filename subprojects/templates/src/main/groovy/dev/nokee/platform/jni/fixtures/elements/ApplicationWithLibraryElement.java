package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.ApplicationElement;

// TODO: Not sure if it should extends from ApplicationElement
public interface ApplicationWithLibraryElement extends ApplicationElement {
	SourceElement getLibrary();
	SourceElement getApplication();
}
