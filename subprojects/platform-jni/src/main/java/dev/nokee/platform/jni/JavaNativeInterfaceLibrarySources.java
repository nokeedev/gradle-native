package dev.nokee.platform.jni;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.base.internal.BaseFunctionalSourceSet;
import dev.nokee.language.c.internal.CSourceSetExtensible;
import dev.nokee.language.cpp.internal.CppSourceSetExtensible;
import dev.nokee.language.jvm.internal.JvmSourceSetExtensible;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetExtensible;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetExtensible;
import dev.nokee.platform.base.ComponentSources;

/**
 * Sources for a Java Native Interface (JNI) library supporting C, C++, Objective-C, ObjectiveC++, Java, Groovy, and Kotlin implementation language.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @since 0.5
 */
public class JavaNativeInterfaceLibrarySources extends BaseFunctionalSourceSet implements ComponentSources, CSourceSetExtensible, CppSourceSetExtensible, ObjectiveCSourceSetExtensible, ObjectiveCppSourceSetExtensible, JvmSourceSetExtensible {}
