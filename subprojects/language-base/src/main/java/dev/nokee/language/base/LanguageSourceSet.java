package dev.nokee.language.base;

import dev.nokee.model.DomainObjectIdentifier;
import org.gradle.api.Action;
import org.gradle.api.Describable;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.tasks.util.PatternFilterable;

public interface LanguageSourceSet extends Describable, HasPublicType {
	/**
	 * Adds a set of source paths to this collection.
	 * The given paths are evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param paths The files to add.
	 * @return this source set, never null.
	 */
	LanguageSourceSet from(Object... paths);

	/**
	 * Returns the source directories that make up this set, represented as a {@link FileCollection}.
	 * Does not filter source directories that do not exist.
	 *
	 * The return value of this method also maintains dependency information.
	 *
	 * <p>The returned collection is live and reflects changes to this source directory set.
	 *
	 * @return a {@link FileCollection} instance of all the source directories from this source set.
	 */
	FileCollection getSourceDirectories();

	/**
	 * Configures the filter patterns using the specified configuration action.
	 *
	 * @param action the configuration action
	 * @return this language source set, never null.
	 */
	LanguageSourceSet filter(Action<? super PatternFilterable> action);

	/**
	 * Returns the filter used to select the source from the source directories.
	 * These filter patterns are applied after the include and exclude patterns of the specified paths.
	 * Generally, the filter patterns are used to restrict the contents to certain types of files, eg {@code *.java}.
	 *
	 * @return the filter patterns, never null.
	 */
	PatternFilterable getFilter();

	/**
	 * Returns this source set as a filtered file tree.
	 *
	 * @return a {@link FileTree} instance representing all the files included in this source set, never null.
	 */
	FileTree getAsFileTree();

//	/**
//	 * Returns...
//	 * @return
//	 */
//	UTType getType();

	DomainObjectIdentifier getIdentifier();
}
