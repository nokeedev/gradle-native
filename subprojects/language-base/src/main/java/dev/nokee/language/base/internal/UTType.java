package dev.nokee.language.base.internal;

/**
 * An uniform type identifier as describe by Apple.
 * Each UTI provides a unique identifier for a particular file type, data type, directory, bundle type, etc.
 * It is used to strongly type {@link SourceSet} and their content.
 *
 * @see <a href="https://developer.apple.com/library/archive/documentation/FileManagement/Conceptual/understanding_utis/understand_utis_intro/understand_utis_intro.html">Introduction to UTIs</a>
 */
public interface UTType {
	String getIdentifier();

	String[] getFilenameExtensions();

	String getDisplayName();
}
