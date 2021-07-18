package dev.nokee.platform.objectivec;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.nativebase.HasPublicSourceSet;

/**
 * Sources for a native library implemented in Objective-C.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasHeadersSourceSet
 * @see HasObjectiveCSourceSet
 * @see HasPublicSourceSet
 * @since 0.5
 */
public class ObjectiveCLibrarySources extends BaseFunctionalSourceSet implements ComponentSources, HasHeadersSourceSet, HasObjectiveCSourceSet, HasPublicSourceSet {}
