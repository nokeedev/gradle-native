package dev.nokee.language.nativebase;

import org.gradle.api.file.ConfigurableFileCollection;

/**
 * Represents an element that carries native object files.
 *
 * @since 0.5
 */
public interface HasObjectFiles {
	/**
	 * Returns the object files produced during the compilation.
	 *
	 * @return a file collection containing the object files, never null.
	 */
	ConfigurableFileCollection getObjectFiles();
}
