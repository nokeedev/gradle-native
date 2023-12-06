/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.language.objectivec.internal;

import dev.nokee.language.base.internal.LanguagePropertiesAware;
import dev.nokee.language.base.internal.LanguageSourcePropertySpec;
import dev.nokee.language.objectivec.HasObjectiveCSources;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;

public interface ObjectiveCSourcesMixIn extends HasObjectiveCSources, LanguagePropertiesAware {
	@Override
	default ConfigurableFileCollection getObjectiveCSources() {
		final String name = ModelObjectIdentifiers.asFullyQualifiedName(getIdentifier().child("objectiveCSources")).toString();
		return getSourceProperties().named(name, LanguageSourcePropertySpec.class).get().getSource();
	}

	@Override
	default void objectiveCSources(Action<? super ConfigurableFileCollection> action) {
		action.execute(getObjectiveCSources());
	}
}
