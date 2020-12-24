package dev.nokee.platform.ios;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.nativebase.HasHeadersSourceSet;
import dev.nokee.platform.objectivec.HasObjectiveCSourceSet;

/**
 * Sources for a iOS application implemented in Objective-C.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasObjectiveCSourceSet
 * @see HasHeadersSourceSet
 * @see HasResourcesSourceSet
 * @since 0.5
 */
public class ObjectiveCIosApplicationSources extends BaseFunctionalSourceSet implements ComponentSources, HasObjectiveCSourceSet, HasHeadersSourceSet, HasResourcesSourceSet {}
