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
package dev.nokee.platform.base.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.core.Bits;
import dev.nokee.model.internal.core.HasInputs;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelActions;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.state.ModelState;
import lombok.val;

import java.util.List;

public final class OnDiscover implements ModelAction, HasInputs {
	private final List<? extends ModelComponentReference<?>> inputs;
	private final Bits inputBits;
	private final ModelAction delegate;

	public OnDiscover(ModelAction delegate) {
		this.delegate = ModelActions.once(delegate);
		val builder = ImmutableList.<ModelComponentReference<?>>builder();
		builder.add(ModelComponentReference.of(ModelState.class));
		if (this.delegate instanceof HasInputs) {
			builder.addAll(((HasInputs) this.delegate).getInputs());
		}
		this.inputs = builder.build();
		this.inputBits = inputs.stream().map(it -> it.componentBits()).reduce(Bits.empty(), Bits::or);
	}

	@Override
	public List<? extends ModelComponentReference<?>> getInputs() {
		return inputs;
	}

	@Override
	public Bits getInputBits() {
		return inputBits;
	}

	@Override
	public void execute(ModelNode entity) {
		if (entity.getComponentBits().containsAll(inputBits)) {
			if (entity.getComponent(ModelComponentType.componentOf(ModelState.class)).isAtLeast(ModelState.Registered)) {
				delegate.execute(entity);
			}
		}
	}
}
