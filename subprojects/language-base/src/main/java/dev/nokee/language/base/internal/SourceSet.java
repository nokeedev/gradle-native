package dev.nokee.language.base.internal;

import org.gradle.api.file.FileTree;

/**
 * An immutable set of source associating a set of directories with filter patterns.
 *
 * @since 0.4
 */
public interface SourceSet {
	FileTree getAsFileTree();

	String getName();

	UTType getType();

	default SourceSet transform(SourceSetTransform transformer) {
		return transformer.transform(this);
	}
}
