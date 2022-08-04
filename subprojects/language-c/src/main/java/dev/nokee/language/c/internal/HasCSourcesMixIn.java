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
package dev.nokee.language.c.internal;

import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.HasCSources;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;

import static dev.nokee.model.internal.type.ModelType.of;

@DomainObjectEntities.Tag(HasCSourcesMixIn.Tag.class)
public interface HasCSourcesMixIn extends HasCSources {
	@Override
	default ConfigurableFileCollection getCSources() {
		return ModelProperties.of(this, CSourcesPropertyComponent.class).asProperty(of(ConfigurableFileCollection.class));
	}

	// For Groovy DSL
	default ConfigurableFileCollection getcSources() {
		return getCSources();
	}

	@Override
	default void cSources(Action<? super ConfigurableFileCollection> action) {
		action.execute(getCSources());
	}

	@Override
	default void cSources(@SuppressWarnings("rawtypes") Closure closure) {
		cSources(new ClosureWrappedConfigureAction<>(closure));
	}

	interface Tag extends ModelTag {}
}
