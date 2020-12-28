package dev.nokee.platform.ios;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.swift.HasSwiftSourceSet;

/**
 * Sources for a iOS application implemented in Swift.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @see HasSwiftSourceSet
 * @see HasResourcesSourceSet
 * @since 0.5
 */
public class SwiftIosApplicationSources extends BaseFunctionalSourceSet implements ComponentSources, HasSwiftSourceSet, HasResourcesSourceSet {}
