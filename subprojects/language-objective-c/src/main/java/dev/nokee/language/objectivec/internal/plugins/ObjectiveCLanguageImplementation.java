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

package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.SourceProperty;
import dev.nokee.language.nativebase.internal.NativeHeaderProperty;
import dev.nokee.language.nativebase.internal.NativeLanguageImplementation;
import dev.nokee.model.internal.names.ElementName;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.function.BiConsumer;

import static dev.nokee.language.base.internal.LanguageImplementation.layout;

public class ObjectiveCLanguageImplementation implements NativeLanguageImplementation {
	private final ObjectFactory objects;

	@Inject
	public ObjectiveCLanguageImplementation(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void registerSourceProperties(LanguagePropertiesAware target) {
		target.getSourceProperties().add(objects.newInstance(SourceProperty.class, "objectiveCSources", Arrays.asList(layout("objectiveC"), layout("objc"))));
		target.getSourceProperties().add(objects.newInstance(NativeHeaderProperty.class, "privateHeaders", NativeHeaderProperty.BasicVisibility.Private));
	}

	@Override
	public void registerSourceSet(BiConsumer<? super ElementName, Class<? extends LanguageSourceSet>> action) {
		action.accept(ElementName.of("objectiveC"), ObjectiveCSourceSetSpec.class);
	}
}
