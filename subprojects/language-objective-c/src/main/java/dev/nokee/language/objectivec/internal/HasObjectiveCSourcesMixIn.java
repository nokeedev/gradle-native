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

import dev.nokee.language.objectivec.HasObjectiveCSources;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;

import static dev.nokee.model.internal.type.ModelType.of;

@DomainObjectEntities.Tag(HasObjectiveCSourcesMixIn.Tag.class)
public interface HasObjectiveCSourcesMixIn extends HasObjectiveCSources {
	@Override
	default ConfigurableFileCollection getObjectiveCSources() {
		return ModelProperties.of(this, ObjectiveCSourcesPropertyComponent.class).asProperty(of(ConfigurableFileCollection.class));
	}

	@Override
	default void objectiveCSources(Action<? super ConfigurableFileCollection> action) {
		action.execute(getObjectiveCSources());
	}

	@Override
	default void objectiveCSources(@SuppressWarnings("rawtypes") Closure closure) {
		objectiveCSources(new ClosureWrappedConfigureAction<>(closure));
	}

	interface Tag extends ModelTag {}
}
