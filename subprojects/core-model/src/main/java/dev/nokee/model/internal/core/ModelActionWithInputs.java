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
package dev.nokee.model.internal.core;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.type.ModelType;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ModelActionWithInputs implements ModelAction {
	@Override
	public void execute(ModelNode node) {
		if (getInputs().stream().allMatch(it -> node.hasComponent(it.getConcreteType()))) {
			execute(node, getInputs().stream().map(it -> node.getComponent(it.getConcreteType())).collect(Collectors.toList()));
		}
	}

	public abstract void execute(ModelNode node, List<?> inputs);

	public abstract List<? extends ModelType<?>> getInputs();


	public static <I0> ModelAction of(ModelType<I0> i0, A1<? super I0> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0));
			}

			@Override
			public List<? extends ModelType<?>> getInputs() {
				return ImmutableList.of(i0);
			}
		};
	}

	public interface A1<I0> {
		void execute(ModelNode node, I0 i0);
	}

	public static <I0, I1> ModelAction of(ModelType<I0> i0, ModelType<I1> i1, A2<? super I0, ? super I1> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0), (I1) inputs.get(1));
			}

			@Override
			public List<? extends ModelType<?>> getInputs() {
				return ImmutableList.of(i0, i1);
			}
		};
	}

	public interface A2<I0, I1> {
		void execute(ModelNode node, I0 i0, I1 i1);
	}

	public static <I0, I1, I2> ModelAction of(ModelType<I0> i0, ModelType<I1> i1, ModelType<I2> i2, A3<? super I0, ? super I1, ? super I2> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2));
			}

			@Override
			public List<? extends ModelType<?>> getInputs() {
				return ImmutableList.of(i0, i1, i2);
			}
		};
	}

	public interface A3<I0, I1, I2> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2);
	}
}
