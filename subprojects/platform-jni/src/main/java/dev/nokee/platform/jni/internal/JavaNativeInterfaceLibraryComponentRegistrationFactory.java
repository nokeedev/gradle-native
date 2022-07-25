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
package dev.nokee.platform.jni.internal;

import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.IsComponent;
import lombok.val;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;

public final class JavaNativeInterfaceLibraryComponentRegistrationFactory {
	public ModelRegistration create(ComponentIdentifier identifier) {
		val builder = ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(IsComponent.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(tag(JniLibraryComponentTag.class))
			.mergeFrom(tagsOf(JniLibraryComponentInternal.class))
			.withComponent(managed(of(JniLibraryComponentInternal.class)))
			;

		if (identifier.isMainComponent()) {
			builder.withComponent(tag(ExcludeFromQualifyingNameTag.class));
		}

		return builder.build();
	}
}
