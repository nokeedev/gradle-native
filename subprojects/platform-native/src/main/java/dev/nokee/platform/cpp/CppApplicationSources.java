package dev.nokee.platform.cpp;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;

/**
 * Sources for a native application implemented in C++.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasHeadersSourceSet
 * @see HasCppSourceSet
 * @since 0.5
 */
public class CppApplicationSources extends BaseFunctionalSourceSet implements ComponentSources, HasHeadersSourceSet, HasCppSourceSet {}
