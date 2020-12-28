package dev.nokee.language.nativebase;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SelfAwareLanguageSourceSet;

/**
 * A set of native header files.
 * <p>
 * Include roots, also known as header search paths, of this source set are accessible via {@link #getSourceDirectories()}.
 *
 * @see LanguageSourceSet
 * @since 0.5
 */
public interface NativeHeaderSet extends SelfAwareLanguageSourceSet<NativeHeaderSet> {}
