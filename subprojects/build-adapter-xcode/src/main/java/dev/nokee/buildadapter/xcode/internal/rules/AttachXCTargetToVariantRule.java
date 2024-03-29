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
package dev.nokee.buildadapter.xcode.internal.rules;

import dev.nokee.buildadapter.xcode.internal.components.XCTargetComponent;
import dev.nokee.model.capabilities.variants.LinkedVariantsComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;

public final class AttachXCTargetToVariantRule extends ModelActionWithInputs.ModelAction2<XCTargetComponent, LinkedVariantsComponent> {
	@Override
	protected void execute(ModelNode entity, XCTargetComponent target, LinkedVariantsComponent variants) {
		for (ModelNode variant : variants) {
			variant.addComponent(target);
		}
	}
}
