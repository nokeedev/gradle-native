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
package dev.nokee.model.internal.plugins;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;

final class GenerateModelPathFromIdentifier extends ModelActionWithInputs.ModelAction1<DomainObjectIdentifier> {
	public GenerateModelPathFromIdentifier() {
		super(ModelComponentReference.ofAny(ModelComponentType.componentOf(DomainObjectIdentifier.class)));
	}

	@Override
	protected void execute(ModelNode entity, DomainObjectIdentifier identifier) {
		entity.addComponent(DomainObjectIdentifierUtils.toPath(identifier));
	}
}
