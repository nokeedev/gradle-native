package dev.nokee.platform.objectivec;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;

/**
 * Sources for a native application implemented in Objective-C.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasHeadersSourceSet
 * @see HasObjectiveCSourceSet
 * @since 0.5
 */
public class ObjectiveCApplicationSources extends BaseFunctionalSourceSet implements ComponentSources, HasHeadersSourceSet, HasObjectiveCSourceSet {}
