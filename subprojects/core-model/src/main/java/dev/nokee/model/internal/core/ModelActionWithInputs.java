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
import dev.nokee.model.internal.buffers.ModelBufferComponent;
import dev.nokee.model.internal.buffers.ModelBufferElement;
import dev.nokee.model.internal.buffers.ModelBuffers;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;
import lombok.val;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ModelActionWithInputs implements ModelAction, HasInputs {
	@Override
	public final void execute(ModelNode node) {
		if (node.getComponentBits().containsAll(getInputBits())) {
			execute(node, getInputs().stream().map(it -> it.get(node)).collect(Collectors.toList()));
		}
	}

	public abstract void execute(ModelNode node, List<?> inputs);

	public abstract List<? extends ModelComponentReference<?>> getInputs();

	private static String displayName(ModelAction self) {
		return Optional.ofNullable(self.getClass().getCanonicalName()).orElse("rule");
	}


	public static <I0 extends ModelComponent> ModelAction of(ModelComponentReference<I0> i0, A1<? super I0> action) {
		return new ModelAction1<I0>(i0) {
			@Override
			protected void execute(ModelNode entity, I0 i0) {
				action.execute(entity, i0);
			}
		};
	}

	public interface A1<I0> {
		void execute(ModelNode node, I0 i0);
	}

	public static abstract class ModelAction1<I0 extends ModelComponent> implements ModelAction, HasInputs {
		private final ModelComponentReference<I0> i0;
		private final List<ModelComponentReference<I0>> inputs;
		private final Bits inputBits;

		protected ModelAction1() {
			this.i0 = referenceOf(new TypeToken<I0>(getClass()) {});
			this.inputs = ImmutableList.of(i0);
			this.inputBits = i0.componentBits();
		}

		protected ModelAction1(ModelComponentReference<I0> i0) {
			this.i0 = i0;
			this.inputs = ImmutableList.of(i0);
			this.inputBits = i0.componentBits();
		}

		@Override
		public final void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				execute(node, i0.get(node));
			}
		}

		protected abstract void execute(ModelNode entity, I0 i0);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		@Override
		public String toString() {
			return displayName(this) + " (" + inputs.stream().map(ModelComponentReference::getType).map(Objects::toString).collect(Collectors.joining(", ")) + ")";
		}
	}

	public static <I0 extends ModelComponent, I1 extends ModelComponent> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, A2<? super I0, ? super I1> action) {
		return new ModelAction2<I0, I1>(i0, i1) {
			@Override
			protected void execute(ModelNode entity, I0 i0, I1 i1) {
				action.execute(entity, i0, i1);
			}
		};
	}

	public interface A2<I0, I1> {
		void execute(ModelNode node, I0 i0, I1 i1);
	}

	public static abstract class ModelAction2<I0 extends ModelComponent, I1 extends ModelComponent> implements ModelAction, HasInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		protected ModelAction2() {
			this.i0 = referenceOf(new TypeToken<I0>(getClass()) {});
			this.i1 = referenceOf(new TypeToken<I1>(getClass()) {});
			this.inputs = ImmutableList.of(i0, i1);
			this.inputBits = i0.componentBits().or(i1.componentBits());
		}

		protected ModelAction2(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1) {
			this.i0 = i0;
			this.i1 = i1;
			this.inputs = ImmutableList.of(i0, i1);
			this.inputBits = i0.componentBits().or(i1.componentBits());
		}

		@Override
		public final void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				execute(node, i0.get(node), i1.get(node));
			}
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		@Override
		public String toString() {
			return displayName(this) + " (" + inputs.stream().map(ModelComponentReference::getType).map(Objects::toString).collect(Collectors.joining(", ")) + ")";
		}
	}

	public static <I0 extends ModelComponent, I1 extends ModelComponent, I2 extends ModelComponent> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, A3<? super I0, ? super I1, ? super I2> action) {
		return new ModelAction3<I0, I1, I2>(i0, i1, i2) {
			@Override
			protected void execute(ModelNode entity, I0 i0, I1 i1, I2 i2) {
				action.execute(entity, i0, i1, i2);
			}
		};
	}

	public interface A3<I0, I1, I2> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2);
	}

	public static abstract class ModelAction3<I0 extends ModelComponent, I1 extends ModelComponent, I2 extends ModelComponent> implements ModelAction, HasInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final ModelComponentReference<I2> i2;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		protected ModelAction3() {
			this.i0 = referenceOf(new TypeToken<I0>(getClass()) {});
			this.i1 = referenceOf(new TypeToken<I1>(getClass()) {});
			this.i2 = referenceOf(new TypeToken<I2>(getClass()) {});
			this.inputs = ImmutableList.of(i0, i1, i2);
			this.inputBits = i0.componentBits().or(i1.componentBits()).or(i2.componentBits());
		}

		protected ModelAction3(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2) {
			this.i0 = i0;
			this.i1 = i1;
			this.i2 = i2;
			this.inputs = ImmutableList.of(i0, i1, i2);
			this.inputBits = i0.componentBits().or(i1.componentBits()).or(i2.componentBits());
		}

		@Override
		public final void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				execute(node, i0.get(node), i1.get(node), i2.get(node));
			}
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1, I2 i2);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		@Override
		public String toString() {
			return displayName(this) + " (" + inputs.stream().map(ModelComponentReference::getType).map(Objects::toString).collect(Collectors.joining(", ")) + ")";
		}
	}

	public static <I0 extends ModelComponent, I1 extends ModelComponent, I2 extends ModelComponent, I3 extends ModelComponent> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, A4<? super I0, ? super I1, ? super I2, ? super I3> action) {
		return new ModelAction4<I0, I1, I2, I3>(i0, i1, i2, i3) {
			@Override
			protected void execute(ModelNode entity, I0 i0, I1 i1, I2 i2, I3 i3) {
				action.execute(entity, i0, i1, i2, i3);
			}
		};
	}

	public interface A4<I0, I1, I2, I3> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2, I3 i3);
	}

	public static abstract class ModelAction4<I0 extends ModelComponent, I1 extends ModelComponent, I2 extends ModelComponent, I3 extends ModelComponent> implements ModelAction, HasInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final ModelComponentReference<I2> i2;
		private final ModelComponentReference<I3> i3;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		protected ModelAction4() {
			this.i0 = referenceOf(new TypeToken<I0>(getClass()) {});
			this.i1 = referenceOf(new TypeToken<I1>(getClass()) {});
			this.i2 = referenceOf(new TypeToken<I2>(getClass()) {});
			this.i3 = referenceOf(new TypeToken<I3>(getClass()) {});
			this.inputs = ImmutableList.of(i0, i1, i2, i3);
			this.inputBits = i0.componentBits().or(i1.componentBits()).or(i2.componentBits()).or(i3.componentBits());
		}

		protected ModelAction4(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3) {
			this.i0 = i0;
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
			this.inputs = ImmutableList.of(i0, i1, i2, i3);
			this.inputBits = i0.componentBits().or(i1.componentBits()).or(i2.componentBits()).or(i3.componentBits());
		}

		@Override
		public final void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				execute(node, i0.get(node), i1.get(node), i2.get(node), i3.get(node));
			}
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1, I2 i2, I3 i3);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		@Override
		public String toString() {
			return displayName(this) + " (" + inputs.stream().map(ModelComponentReference::getType).map(Objects::toString).collect(Collectors.joining(", ")) + ")";
		}
	}

	public static <I0 extends ModelComponent, I1 extends ModelComponent, I2 extends ModelComponent, I3 extends ModelComponent, I4 extends ModelComponent> ModelAction of(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, ModelComponentReference<I4> i4, A5<? super I0, ? super I1, ? super I2, ? super I3, ? super I4> action) {
		return new ModelAction5<I0, I1, I2, I3, I4>(i0, i1, i2, i3, i4) {
			@Override
			protected void execute(ModelNode entity, I0 i0, I1 i1, I2 i2, I3 i3, I4 i4) {
				action.execute(entity, i0, i1, i2, i3, i4);
			}
		};
	}

	public interface A5<I0, I1, I2, I3, I4> {
		void execute(ModelNode node, I0 i0, I1 i1, I2 i2, I3 i3, I4 i4);
	}

	public static abstract class ModelAction5<I0 extends ModelComponent, I1 extends ModelComponent, I2 extends ModelComponent, I3 extends ModelComponent, I4 extends ModelComponent> implements ModelAction, HasInputs {
		private final ModelComponentReference<I0> i0;
		private final ModelComponentReference<I1> i1;
		private final ModelComponentReference<I2> i2;
		private final ModelComponentReference<I3> i3;
		private final ModelComponentReference<I4> i4;
		private final List<ModelComponentReference<?>> inputs;
		private final Bits inputBits;

		protected ModelAction5() {
			this.i0 = referenceOf(new TypeToken<I0>(getClass()) {});
			this.i1 = referenceOf(new TypeToken<I1>(getClass()) {});
			this.i2 = referenceOf(new TypeToken<I2>(getClass()) {});
			this.i3 = referenceOf(new TypeToken<I3>(getClass()) {});
			this.i4 = referenceOf(new TypeToken<I4>(getClass()) {});
			this.inputs = ImmutableList.of(i0, i1, i2, i3, i4);
			this.inputBits = i0.componentBits().or(i1.componentBits()).or(i2.componentBits()).or(i3.componentBits()).or(i4.componentBits());
		}

		protected ModelAction5(ModelComponentReference<I0> i0, ModelComponentReference<I1> i1, ModelComponentReference<I2> i2, ModelComponentReference<I3> i3, ModelComponentReference<I4> i4) {
			this.i0 = i0;
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
			this.i4 = i4;
			this.inputs = ImmutableList.of(i0, i1, i2, i3, i4);
			this.inputBits = i0.componentBits().or(i1.componentBits()).or(i2.componentBits()).or(i3.componentBits()).or(i4.componentBits());
		}
		@Override
		public final void execute(ModelNode node) {
			if (node.getComponentBits().containsAll(inputBits)) {
				execute(node, i0.get(node), i1.get(node), i2.get(node), i3.get(node), i4.get(node));
			}
		}

		protected abstract void execute(ModelNode entity, I0 i0, I1 i1, I2 i2, I3 i3, I4 i4);

		@Override
		public final List<? extends ModelComponentReference<?>> getInputs() {
			return inputs;
		}

		@Override
		public Bits getInputBits() {
			return inputBits;
		}

		@Override
		public String toString() {
			return displayName(this) + " (" + inputs.stream().map(ModelComponentReference::getType).map(Objects::toString).collect(Collectors.joining(", ")) + ")";
		}
	}

	private static <T extends ModelComponent> ModelComponentReference<T> referenceOf(TypeToken<T> type) {
		if (type.isSubtypeOf(ModelComponentTag.class)) {
			@SuppressWarnings("unchecked")
			val result = (ModelComponentReference<T>) ModelTags.referenceOf((Class<? extends ModelTag>) ((ParameterizedType) type.getType()).getActualTypeArguments()[0]);
			return result;
		} else if (type.isSubtypeOf(ModelBufferComponent.class)) {
			@SuppressWarnings("unchecked")
			val result = (ModelComponentReference<T>) ModelBuffers.referenceOf((Class<? extends ModelBufferElement>) ((ParameterizedType) type.getType()).getActualTypeArguments()[0]);
			return result;
		} else if (type.isSubtypeOf(TypeCompatibilityModelProjectionSupport.class)) {
			@SuppressWarnings("unchecked")
			val result = (ModelComponentReference<T>) ModelProjections.referenceOf((Class<?>) ((ParameterizedType) type.getType()).getActualTypeArguments()[0]);
			return result;
		} else {
			@SuppressWarnings("unchecked")
			val componentType = (Class<T>) type.getRawType();
			return ModelComponentReference.of(componentType);
		}
	}
}
