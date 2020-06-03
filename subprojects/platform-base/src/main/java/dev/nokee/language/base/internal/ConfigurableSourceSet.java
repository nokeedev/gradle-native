package dev.nokee.language.base.internal;

/**
 * A mutable {@link SourceSet}.
 *
 * @since 0.4
 */
public interface ConfigurableSourceSet extends SourceSet {
	ConfigurableSourceSet srcDir(Object srcPath);
}
