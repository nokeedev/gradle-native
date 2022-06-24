/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.model.internal.DomainObjectEntities;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.tags.ModelTag;

@DomainObjectEntities.Tag(HasConfigurableSourceMixIn.Tag.class)
public interface HasConfigurableSourceMixIn extends HasConfigurableSource {
	default ConfigurableSourceSet getSource() {
		return ModelElements.of(this, SourcePropertyComponent.class).as(ConfigurableSourceSet.class).get();
	}

	interface Tag extends ModelTag {}
}
