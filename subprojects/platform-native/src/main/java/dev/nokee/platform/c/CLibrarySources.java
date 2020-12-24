package dev.nokee.platform.c;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.HasPublicSourceSet;

/**
 * Sources for a native library implemented in C.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasHeadersSourceSet
 * @see HasCSourceSet
 * @since 0.5
 */
public class CLibrarySources extends BaseFunctionalSourceSet implements ComponentSources, HasHeadersSourceSet, HasCSourceSet, HasPublicSourceSet {}
