package dev.nokee.platform.jni;

import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

/**
 * Configuration for a specific Java Native Interface (JNI) library variant, defining the dependencies that make up the library plus other settings.
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

	/**
	 * Returns the shared library binary built for this variant.
	 *
	 * @return a {@link SharedLibraryBinary} instance, never null.
	 * @since 0.3
	 */
	SharedLibraryBinary getSharedLibrary();

	/**
	 * Configure the shared library binary for this variant.
	 *
	 * @param action configuration action for {@link SharedLibraryBinary}.
	 * @since 0.3
	 */
	void sharedLibrary(Action<? super SharedLibraryBinary> action);

	/**
	 * Configure the binaries of this variant.
	 * The view contains only the binaries participating to this variant.
	 *
	 * @return a {@link BinaryView} for configuring each binary, never null.
	 * @since 0.3
	 */
	BinaryView<Binary> getBinaries();
}
