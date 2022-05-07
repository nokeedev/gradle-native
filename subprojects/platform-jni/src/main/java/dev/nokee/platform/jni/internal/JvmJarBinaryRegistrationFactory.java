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
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.IsBinary;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;

public final class JvmJarBinaryRegistrationFactory {
	public ModelRegistration.Builder create(BinaryIdentifier identifier) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(new ElementNameComponent(identifier.getName().get()))
			.withComponent(tag(IsBinary.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(createdUsing(of(ModelBackedJvmJarBinary.class), ModelBackedJvmJarBinary::new));
	}
}
