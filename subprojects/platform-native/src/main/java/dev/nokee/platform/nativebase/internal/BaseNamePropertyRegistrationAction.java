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
package dev.nokee.platform.nativebase.internal;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import lombok.val;
import org.gradle.api.provider.Property;

@AutoFactory
public final class BaseNamePropertyRegistrationAction extends ModelActionWithInputs.ModelAction2<BinaryIdentifier<?>, ModelState.IsAtLeastRegistered> {
	private final BinaryIdentifier<?> identifier;
	private final ModelRegistry registry;
	private final ModelPropertyRegistrationFactory propertyRegistrationFactory;

	public BaseNamePropertyRegistrationAction(BinaryIdentifier<?> identifier, @Provided ModelRegistry registry, @Provided ModelPropertyRegistrationFactory propertyRegistrationFactory) {
		this.identifier = identifier;
		this.registry = registry;
		this.propertyRegistrationFactory = propertyRegistrationFactory;
	}

	@Override
	protected void execute(ModelNode entity, BinaryIdentifier<?> identifier, ModelState.IsAtLeastRegistered ignored) {
		if (identifier.equals(this.identifier)) {
			val baseNameProperty = registry.register(propertyRegistrationFactory.createProperty(ModelPropertyIdentifier.of(identifier, "baseName"), String.class));
			baseNameProperty.configure(Property.class, it -> it.convention(identifier.getName().get()));
			entity.addComponent(new BaseName(baseNameProperty.as(String.class)));
		}
	}
}
