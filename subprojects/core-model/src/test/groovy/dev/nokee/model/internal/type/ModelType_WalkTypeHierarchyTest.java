/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.type;

import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.mockito.Mockito.*;

class ModelType_WalkTypeHierarchyTest {
	private final ModelType.Visitor<?> visitor = mock(ModelType.Visitor.class);

	@SuppressWarnings("unchecked")
	private <T> ModelType.Visitor<T> visitor() {
		return (ModelType.Visitor<T>) visitor;
	}

	@AfterEach
	void ensureNoMoreInteractionOnVisitor() {
		verifyNoMoreInteractions(visitor);
	}

	@Test
	void walkingTypeHierarchyHappensBreadthFirst() {
		ModelType.Visitor<Child> visitor = visitor();
		of(Child.class).walkTypeHierarchy(visitor);

		val inOrder = inOrder(visitor);
		inOrder.verify(visitor, times(1)).visitType(of(Child.class));
		inOrder.verify(visitor, times(1)).visitType(of(ConcreteBase.class));
		inOrder.verify(visitor, times(1)).visitType(of(Serializable.class));
		inOrder.verify(visitor, times(1)).visitType(of(Iface.class));
		inOrder.verify(visitor, times(1)).visitType(of(Base.class));
		inOrder.verify(visitor, times(1)).visitType(of(Iface1.class));
		inOrder.verify(visitor, times(1)).visitType(of(Iface2.class));
	}

	static class Base {}
	interface Iface {}
	interface Iface1 {}
	interface Iface2 {}
	static class ConcreteBase extends Base implements Iface1, Iface2 {}
	static class Child extends ConcreteBase implements Serializable, Iface {}

	@Test
	void canVisitClassWithoutHierarchy() {
		ModelType.Visitor<Base> visitor = visitor();
		of(Base.class).walkTypeHierarchy(visitor);
		verify(visitor, times(1)).visitType(of(Base.class));
	}

	@Test
	void canVisitInterfaceWithoutHierarchy() {
		ModelType.Visitor<Iface> visitor = visitor();
		of(Iface.class).walkTypeHierarchy(visitor);
		verify(visitor, times(1)).visitType(of(Iface.class));
	}

	@Test
	void canVisitClassImplementingInterface() {
		ModelType.Visitor<SerializableBase> visitor = visitor();
		of(SerializableBase.class).walkTypeHierarchy(visitor);
		verify(visitor, times(1)).visitType(of(SerializableBase.class));
		verify(visitor, times(1)).visitType(of(Serializable.class));
	}

	static class SerializableBase implements Serializable {}

	@Test
	void canVisitDeepParameterizedType() {
		ModelType.Visitor<ConcreteView> visitor = visitor();
		of(ConcreteView.class).walkTypeHierarchy(visitor);

		val inOrder = inOrder(visitor);
		inOrder.verify(visitor, times(1)).visitType(of(ConcreteView.class));
		inOrder.verify(visitor, times(1)).visitType(of(IConcreteView.class));
		inOrder.verify(visitor, times(1)).visitType(of(new TypeOf<IView<Iface>>() {}));
	}

	interface IView<T> {}
	interface IConcreteView extends IView<Iface> {}
	static class ConcreteView implements IConcreteView {}

	@Test
	void doesNotVisitObjectClass() {
		ModelType.Visitor<Object> visitor = visitor();
		of(Object.class).walkTypeHierarchy(visitor);
	}
}
