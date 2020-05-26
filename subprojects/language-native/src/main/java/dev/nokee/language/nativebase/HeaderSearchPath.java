package dev.nokee.language.nativebase;

import java.io.File;

/**
 * Represent of entry in the header search path.
 * The entry can be a user, system, or framework search path.
 *
 * @since 0.3
 */
public interface HeaderSearchPath {
	/**
	 * Returns the location of this search path, as an absolute {@link File}.
	 *
	 * @return a absolute {@link File} representing the location of the search path.
	 */
	File getAsFile();
}
