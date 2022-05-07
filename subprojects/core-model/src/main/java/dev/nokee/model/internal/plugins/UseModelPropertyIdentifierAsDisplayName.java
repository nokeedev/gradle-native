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

import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPropertyTag;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;

final class UseModelPropertyIdentifierAsDisplayName extends ModelActionWithInputs.ModelAction2<IdentifierComponent, ModelComponentTag<ModelPropertyTag>> {
	public UseModelPropertyIdentifierAsDisplayName() {
		super(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(ModelPropertyTag.class));
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<ModelPropertyTag> tag) {
		entity.addComponent(new DisplayNameComponent(new DisplayName(identifier.get().toString())));
	}
}
