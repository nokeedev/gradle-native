package dev.nokee.language.nativebase;

import org.gradle.api.file.DirectoryProperty;

/**
 * Represents an element that carries a destination directory.
 *
 * @since 0.5
 */
public interface HasDestinationDirectory {
	DirectoryProperty getDestinationDirectory();
}
