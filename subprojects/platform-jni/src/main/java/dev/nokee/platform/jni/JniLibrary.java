package dev.nokee.platform.jni;

import org.gradle.api.provider.Property;

/**
 *  Configuration for a specific Java Native Interface (JNI) library variant, defining the dependencies that make up the library plus other settings.
 *
 * @since 0.2
 */
public interface JniLibrary {
	/**
	 * Specifies the resource path where the native components of the JNI library will be located within the JAR.
	 *
	 * @return a property for configuring the resource path.
	 */
	Property<String> getResourcePath();
}
