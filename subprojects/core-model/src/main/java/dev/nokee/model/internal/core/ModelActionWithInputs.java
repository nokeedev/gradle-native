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
import com.google.common.reflect.TypeToken;
import dev.nokee.model.internal.type.ModelType;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ModelActionWithInputs implements ModelAction, HasInputs {
	@Override
	public final void execute(ModelNode node) {
		if (getInputs().stream().allMatch(it -> ((ModelComponentReferenceInternal) it).isSatisfiedBy(node.getComponentTypes()))) {
			execute(node, getInputs().stream().map(it -> it.get(node)).collect(Collectors.toList()));
		}
	}

	public abstract void execute(ModelNode node, List<?> inputs);

	public abstract List<? extends ModelComponentReference<?>> getInputs();


	public static <I0> ModelAction of(ModelComponentReference<I0> i0, A1<? super I0> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0));
			}

			@Override
			public List<? extends ModelComponentReference<?>> getInputs() {
				return ImmutableList.of(i0);
			}
		};
	}

	public interface A1<I0> {
		void execute(ModelNode node, I0 i0);
	}

	public static abstract class ModelAction1<I0> extends ModelActionWithInputs {
		private final ModelComponentReference<I0> i0 = ModelComponentReference.of((Class<I0>)new TypeToken<I0>(getClass()) {}.getRawType());

		@Override
		public final void execute(ModelNode node, List<?> inputs) {
			execute(node, (I0) inputs.get(0));
		}

		protected abstract void execute(ModelNode entity, I0 i0);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return ImmutableList.of(i0);
		}
	}

	public static <I0, I1> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, A2<? super I0, ? super I1> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0), (I1) inputs.get(1));
			}

			@Override
			public List<? extends ModelComponentReference<?>> getInputs() {
				return ImmutableList.of(i0, i1);
			}
		};
	}

	public interface A2<I0, I1> {
		void execute(ModelNode node, I0 i0, I1 i1);
	}

	public static <I0, I1, I2> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, A3<? super I0, ? super I1, ? super I2> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2));
			}

			@Override
			public List<? extends ModelComponentReference<?>> getInputs() {
				return ImmutableList.of(i0, i1, i2);
			}
		};
	}

	public interface A3<I0, I1, I2> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2);
	}

	public static <I0, I1, I2, I3> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, A4<? super I0, ? super I1, ? super I2, ? super I3> action) {
		return new ModelActionWithInputs() {
			@Override
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2), (I3) inputs.get(3));
			}

			@Override
			public List<? extends ModelComponentReference<?>> getInputs() {
				return ImmutableList.of(i0, i1, i2, i3);
			}
		};
	}

	public interface A4<I0, I1, I2, I3> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2, I3 i3);
	}
}
