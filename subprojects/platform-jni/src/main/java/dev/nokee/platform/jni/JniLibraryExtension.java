package dev.nokee.platform.jni;

import org.gradle.api.Action;

/**
 * Configuration for a Java Native Interface (JNI) library, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the JNI Library Plugin.</p>
 *
 * @since 0.1
 */
public interface JniLibraryExtension {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link JniLibraryDependencies}, never null.
	 * @since 0.1
	 */
	JniLibraryDependencies getDependencies();

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param action configuration action for {@link JniLibraryDependencies}.
	 * @since 0.1
	 */
	void dependencies(Action<? super JniLibraryDependencies> action);
}
