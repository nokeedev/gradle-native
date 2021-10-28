package dev.nokee.language.base;

import org.gradle.api.Buildable;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;

/**
 * Represent a logical grouping of several files or directories.
 *
 * WARNING: It is not to be confused with Gradle's {@link org.gradle.api.tasks.SourceSet} which is closer to Nokee's {@link LanguageSourceSet}.
 * @since 0.5
 */
public interface SourceSet extends Buildable {
	/**
	 * Returns this source set as a filtered file tree.
	 *
	 * @return a {@link FileTree} instance representing all the files included in this source set, never null
	 */
	FileTree getAsFileTree();

	/**
	 * Returns the source directories that make up this set, represented as a {@link FileCollection}.
	 * Does not filter source directories that do not exist.
	 *
	 * <p>The return value of this method also maintains dependency information.
	 *
	 * <p>The returned collection is live and reflects changes to this source directory set.
	 *
	 * @return a {@link FileCollection} instance of all the source directories from this source set, never null
	 */
	FileCollection getSourceDirectories();
}
