package dev.nokee.ide.xcode;

import org.gradle.api.Named;
import org.gradle.api.file.ConfigurableFileCollection;

/**
 * Represents a Xcode IDE group of sources.
 *
 * @since 0.5
 */
public interface XcodeIdeGroup extends Named {
	/**
	 * Configures the sources of this group.
	 *
	 * @return a file collection to configure the sources for this group, never null.
	 */
	ConfigurableFileCollection getSources();
}
