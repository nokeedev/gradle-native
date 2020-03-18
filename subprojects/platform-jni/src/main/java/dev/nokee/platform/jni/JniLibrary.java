package dev.nokee.platform.jni;

import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.TargetMachine;
import org.gradle.api.provider.Property;

/**
 *  Configuration for a specific Java Native Interface (JNI) library variant, defining the dependencies that make up the library plus other settings.
 *
 * @since 0.2
 */
public interface JniLibrary extends Variant {
	/**
	 * Specifies the resource path where the native components of the JNI library will be located within the JAR.
	 *
	 * @return a property for configuring the resource path, never null.
	 */
	Property<String> getResourcePath();

	/**
	 * Returns the target machine for this variant.
	 *
	 * @return a {@link TargetMachine} instance, never null.
	 */
	TargetMachine getTargetMachine();
}
