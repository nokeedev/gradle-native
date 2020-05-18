package dev.nokee.language.base.internal;

import org.gradle.api.file.FileTree;

/**
 * An immutable set of source associating a set of directories with filter patterns.
 *
 * @param <T> the {@link UTType} of the source set.
 * @since 0.4
 */
public interface SourceSet<T extends UTType> {
	FileTree getAsFileTree();

	default <R extends UTType> SourceSet<R> transform(SourceSetTransform<T, R> transformer) {
		return transformer.transform(this);
	}
}
