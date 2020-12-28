package dev.nokee.language.base;

import dev.nokee.language.base.internal.LanguageSourceSetProjection;
import dev.nokee.model.internal.core.ModelNodes;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.Buildable;
import org.gradle.api.Named;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.util.ConfigureUtil;

/**
 * A set of sources for a programming language.
 *
 * @since 0.5
 */
public interface LanguageSourceSet extends Buildable, Named {
	/**
	 * Returns the name that identify this source set.
	 *
	 * @return the name of the source set, never null
	 */
	default String getName() {
		return ModelNodes.of(this).getPath().getName();
	}

	/**
	 * Adds a set of source paths to this source set.
	 * The given paths are evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param paths  the files to add
	 * @return this source set, never null
	 */
	default LanguageSourceSet from(Object... paths) {
		ModelNodes.of(this).get(LanguageSourceSetProjection.class).from(paths);
		return this;
	}

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
	default FileCollection getSourceDirectories() {
		return ModelNodes.of(this).get(LanguageSourceSetProjection.class).getSourceDirectories();
	}

	/**
	 * Configures the filter patterns using the specified configuration action.
	 *
	 * @param action  the configuration action, must not be null
	 * @return this language source set, never null
	 */
	default LanguageSourceSet filter(Action<? super PatternFilterable> action) {
		action.execute(getFilter());
		return this;
	}

	/**
	 * Configures the filter patterns using the specified configuration closure.
	 *
	 * @param closure  the configuration closure, must not be null
	 * @return this language source set, never null
	 */
	default LanguageSourceSet filter(@DelegatesTo(value = PatternFilterable.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		return filter(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns the filter used to select the source from the source directories.
	 * These filter patterns are applied after the include and exclude patterns of the specified paths.
	 * Generally, the filter patterns are used to restrict the contents to certain types of files, eg {@code *.cpp}.
	 *
	 * @return the filter patterns, never null
	 */
	default PatternFilterable getFilter() {
		return ModelNodes.of(this).get(LanguageSourceSetProjection.class).getFilter();
	}

	/**
	 * Returns this source set as a filtered file tree.
	 *
	 * @return a {@link FileTree} instance representing all the files included in this source set, never null
	 */
	default FileTree getAsFileTree() {
		return ModelNodes.of(this).get(LanguageSourceSetProjection.class).getAsFileTree();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default TaskDependency getBuildDependencies() {
		return ModelNodes.of(this).get(LanguageSourceSetProjection.class).getBuildDependencies();
	}

	/**
	 * Configures a set of source paths to use as a convention of this source set.
	 * The given paths are evaluated as per {@link org.gradle.api.Project#files(Object...)}.
	 *
	 * @param path  the files to use as convention
	 * @return this source set, never null
	 */
	default LanguageSourceSet convention(Object... path) {
		ModelNodes.of(this).get(LanguageSourceSetProjection.class).convention(path);
		return this;
	}
}
