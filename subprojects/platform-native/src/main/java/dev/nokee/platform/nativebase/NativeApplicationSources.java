package dev.nokee.platform.nativebase;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.language.c.internal.CSourceSetExtensible;
import dev.nokee.language.cpp.internal.CppSourceSetExtensible;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetExtensible;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetExtensible;
import dev.nokee.platform.base.ComponentSources;

/**
 * Sources for a native application supporting C, C++, Objective-C, and ObjectiveC++ implementation language.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @since 0.5
 */
public class NativeApplicationSources extends BaseFunctionalSourceSet implements ComponentSources, CSourceSetExtensible, CppSourceSetExtensible, ObjectiveCSourceSetExtensible, ObjectiveCppSourceSetExtensible {}
