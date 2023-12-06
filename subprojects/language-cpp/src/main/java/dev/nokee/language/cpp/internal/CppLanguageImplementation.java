/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.language.cpp.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.PropertySpec;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.nativebase.internal.NativeHeaderProperty;
import dev.nokee.language.nativebase.internal.NativeLanguageImplementation;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.function.BiConsumer;

@EqualsAndHashCode
public class CppLanguageImplementation implements NativeLanguageImplementation {
	@EqualsAndHashCode.Exclude private final ObjectFactory objects;
	@EqualsAndHashCode.Exclude private final ModelObjectRegistry<PropertySpec> propertyRegistry;

	@Inject
	public CppLanguageImplementation(ObjectFactory objects, ModelObjectRegistry<PropertySpec> propertyRegistry) {
		this.objects = objects;
		this.propertyRegistry = propertyRegistry;
	}

	@Override
	public void registerSourceProperties(LanguagePropertiesAware target) {
		val cppSources = propertyRegistry.register(target.getIdentifier().child("cppSources"), SourceProperty.class);
		val privateHeaders = propertyRegistry.register(target.getIdentifier().child("privateHeaders"), NativeHeaderProperty.class);
		privateHeaders.configure(it -> it.getVisibility().set(NativeHeaderProperty.BasicVisibility.Private));

		target.getSourceProperties().add(cppSources.get());
		target.getSourceProperties().add(privateHeaders.get());
	}

	@Override
	public void registerSourceSet(BiConsumer<? super ElementName, Class<? extends LanguageSourceSet>> action) {
		action.accept(ElementName.of("cpp"), CppSourceSetSpec.class);
	}
}
