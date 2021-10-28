package dev.nokee.language.nativebase;

import org.gradle.api.file.ConfigurableFileCollection;

/**
 * Represents an element that carries native object files.
 *
 * @since 0.5
 */
public interface HasObjectFiles {
	ConfigurableFileCollection getObjectFiles();
}
