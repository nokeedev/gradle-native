package dev.nokee.platform.c;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;

/**
 * Sources for a native application implemented in C.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasHeadersSourceSet
 * @see HasCSourceSet
 * @since 0.5
 */
public class CApplicationSources extends BaseFunctionalSourceSet implements ComponentSources, HasHeadersSourceSet, HasCSourceSet {}
