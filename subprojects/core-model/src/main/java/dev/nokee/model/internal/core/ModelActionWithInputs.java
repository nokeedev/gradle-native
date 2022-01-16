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
			@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public static abstract class ModelAction1<I0> extends ModelActionWithInputs {
		private final ModelComponentReference<I0> i0;

		protected ModelAction1() {
			this.i0 = ModelComponentReference.of((Class<I0>)new TypeToken<I0>(getClass()) {}.getRawType());
		}

		protected ModelAction1(ModelComponentReference<I0> i0) {
			this.i0 = i0;
		}

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
			@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public static abstract class ModelAction2<I0, I1> extends ModelActionWithInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;

		protected ModelAction2() {
			this.i0 = ModelComponentReference.of((Class<I0>)new TypeToken<I0>(getClass()) {}.getRawType());
			this.i1 = ModelComponentReference.of((Class<I1>)new TypeToken<I1>(getClass()) {}.getRawType());
		}

		protected ModelAction2(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1) {
			this.i0 = i0;
			this.i1 = i1;
		}

		@Override
		public final void execute(ModelNode node, List<?> inputs) {
			execute(node, (I0) inputs.get(0), (I1) inputs.get(1));
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return ImmutableList.of(i0, i1);
		}
	}

	public static <I0, I1, I2> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, A3<? super I0, ? super I1, ? super I2> action) {
		return new ModelActionWithInputs() {
			@Override
			@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public static abstract class ModelAction3<I0, I1, I2> extends ModelActionWithInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final ModelComponentReference<I2> i2;

		protected ModelAction3() {
			this.i0 = ModelComponentReference.of((Class<I0>)new TypeToken<I0>(getClass()) {}.getRawType());
			this.i1 = ModelComponentReference.of((Class<I1>)new TypeToken<I1>(getClass()) {}.getRawType());
			this.i2 = ModelComponentReference.of((Class<I2>)new TypeToken<I2>(getClass()) {}.getRawType());
		}

		protected ModelAction3(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2) {
			this.i0 = i0;
			this.i1 = i1;
			this.i2 = i2;
		}

		@Override
		public final void execute(ModelNode node, List<?> inputs) {
			execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2));
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1, I2 i2);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return ImmutableList.of(i0, i1, i2);
		}
	}

	public static <I0, I1, I2, I3> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, A4<? super I0, ? super I1, ? super I2, ? super I3> action) {
		return new ModelActionWithInputs() {
			@Override
			@SuppressWarnings("unchecked")
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

	@SuppressWarnings("unchecked")
	public static abstract class ModelAction4<I0, I1, I2, I3> extends ModelActionWithInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final ModelComponentReference<I2> i2;
		private final ModelComponentReference<I3> i3;

		protected ModelAction4() {
			this.i0 = ModelComponentReference.of((Class<I0>)new TypeToken<I0>(getClass()) {}.getRawType());
			this.i1 = ModelComponentReference.of((Class<I1>)new TypeToken<I1>(getClass()) {}.getRawType());
			this.i2 = ModelComponentReference.of((Class<I2>)new TypeToken<I2>(getClass()) {}.getRawType());
			this.i3 = ModelComponentReference.of((Class<I3>)new TypeToken<I3>(getClass()) {}.getRawType());
		}

		protected ModelAction4(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3) {
			this.i0 = i0;
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
		}

		@Override
		public final void execute(ModelNode node, List<?> inputs) {
			execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2), (I3) inputs.get(3));
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1, I2 i2, I3 i3);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return ImmutableList.of(i0, i1, i2, i3);
		}
	}

	public static <I0, I1, I2, I3, I4> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, ModelComponentReference<I4> i4, A5<? super I0, ? super I1, ? super I2, ? super I3, ? super I4> action) {
		return new ModelActionWithInputs() {
			@Override
			@SuppressWarnings("unchecked")
			public void execute(ModelNode node, List<?> inputs) {
				action.execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2), (I3) inputs.get(3), (I4) inputs.get(4));
			}

			@Override
			public List<? extends ModelComponentReference<?>> getInputs() {
				return ImmutableList.of(i0, i1, i2, i3, i4);
			}
		};
	}

	public interface A5<I0, I1, I2, I3, I4> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2, I3 i3, I4 i4);
	}

	@SuppressWarnings("unchecked")
	public static abstract class ModelAction5<I0, I1, I2, I3, I4> extends ModelActionWithInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final ModelComponentReference<I2> i2;
		private final ModelComponentReference<I3> i3;
		private final ModelComponentReference<I4> i4;

		protected ModelAction5() {
			this.i0 = ModelComponentReference.of((Class<I0>)new TypeToken<I0>(getClass()) {}.getRawType());
			this.i1 = ModelComponentReference.of((Class<I1>)new TypeToken<I1>(getClass()) {}.getRawType());
			this.i2 = ModelComponentReference.of((Class<I2>)new TypeToken<I2>(getClass()) {}.getRawType());
			this.i3 = ModelComponentReference.of((Class<I3>)new TypeToken<I3>(getClass()) {}.getRawType());
			this.i4 = ModelComponentReference.of((Class<I4>)new TypeToken<I4>(getClass()) {}.getRawType());
		}

		protected ModelAction5(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, ModelComponentReference<I4> i4) {
			this.i0 = i0;
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
			this.i4 = i4;
		}
		@Override
		public final void execute(ModelNode node, List<?> inputs) {
			execute(node, (I0) inputs.get(0), (I1) inputs.get(1), (I2) inputs.get(2), (I3) inputs.get(3), (I4) inputs.get(4));
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1, I2 i2, I3 i3, I4 i4);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return ImmutableList.of(i0, i1, i2, i3, i4);
		}
	}
}
