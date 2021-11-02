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
import dev.nokee.language.jvm.*;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetExtensible;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetExtensible;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelBackedNamedDomainObjectProvider;
import dev.nokee.platform.base.ComponentSources;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.util.ConfigureUtil;

/**
 * Sources for a Java Native Interface (JNI) library supporting C, C++, Objective-C, ObjectiveC++, Java, Groovy, and Kotlin implementation language.
 *
 * @see FunctionalSourceSet
 * @see ComponentSources
 * @since 0.5
 */
public class JavaNativeInterfaceLibrarySources extends BaseFunctionalSourceSet implements ComponentSources
	, CSourceSetExtensible
	, CppSourceSetExtensible
	, ObjectiveCSourceSetExtensible
	, ObjectiveCppSourceSetExtensible
	, HasJavaSourceSet, HasGroovySourceSet, HasKotlinSourceSet {
	public NamedDomainObjectProvider<GroovySourceSet> getGroovy() {
		val result = ModelProperties.findProperty(this, "groovy");
		if (result.isPresent()) {
			return new ModelBackedNamedDomainObjectProvider<>(result.get().as(GroovySourceSet.class));
		}
		throw new RuntimeException("Please apply 'groovy' plugin to access Groovy source set.");
	}

	public void groovy(Action<? super GroovySourceSet> action) {
		getGroovy().configure(action);
	}

	public void groovy(@SuppressWarnings("rawtypes") Closure closure) {
		groovy(ConfigureUtil.configureUsing(closure));
	}

	public NamedDomainObjectProvider<JavaSourceSet> getJava() {
		val result = ModelProperties.findProperty(this, "java");
		if (result.isPresent()) {
			return new ModelBackedNamedDomainObjectProvider<>(result.get().as(JavaSourceSet.class));
		}
		throw new RuntimeException("Please apply 'java' plugin to access Java source set.");
	}

	public void java(Action<? super JavaSourceSet> action) {
		getJava().configure(action);
	}

	public void java(@SuppressWarnings("rawtypes") Closure closure) {
		java(ConfigureUtil.configureUsing(closure));
	}

	public NamedDomainObjectProvider<KotlinSourceSet> getKotlin() {
		val result = ModelProperties.findProperty(this, "kotlin");
		if (result.isPresent()) {
			return new ModelBackedNamedDomainObjectProvider<>(result.get().as(KotlinSourceSet.class));
		}
		throw new RuntimeException("Please apply 'org.jetbrains.kotlin.jvm' plugin to access Kotlin source set.");
	}

	public void kotlin(Action<? super KotlinSourceSet> action) {
		getKotlin().configure(action);
	}

	public void kotlin(@SuppressWarnings("rawtypes") Closure closure) {
		kotlin(ConfigureUtil.configureUsing(closure));
	}
}
