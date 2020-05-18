package dev.nokee.language.base.internal;

/**
 * A mutable {@link SourceSet}.
 *
 * @param <T> the {@link UTType} of the source set.
 * @since 0.4
 */
public interface ConfigurableSourceSet<T extends UTType> extends SourceSet<T> {
	SourceSet<T> srcDir(Object srcPath);
}
