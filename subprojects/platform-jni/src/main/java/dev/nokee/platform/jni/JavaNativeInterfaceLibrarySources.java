/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
