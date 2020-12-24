package dev.nokee.platform.cpp;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.HasPublicSourceSet;

/**
 * Sources for a native library implemented in C++.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasHeadersSourceSet
 * @see HasPublicSourceSet
 * @see HasCppSourceSet
 * @since 0.5
 */
public class CppLibrarySources extends BaseFunctionalSourceSet implements ComponentSources, HasHeadersSourceSet, HasCppSourceSet, HasPublicSourceSet {}
