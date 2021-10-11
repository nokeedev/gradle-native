/*
 * Copyright 2020-2021 the original author or authors.
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

import com.google.common.testing.EqualsTester;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelActions.register;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(ModelActions.class)
class ModelActions_RegisterTest {
	@Test
	void registerNodeOnExecutingNode() {
		val modelRegistry = mock(ModelRegistry.class);
		val node = node("foo", builder -> builder.withRegistry(modelRegistry));
		register(NodeRegistration.of("bar", of(MyType.class))).execute(node);
		verify(modelRegistry, times(1)).register(ModelRegistration.of("foo.bar", MyType.class));
	}

	@Test
	void registerSuppliedNodeRegistrationOnExecutingNode() {
		val modelRegistry = mock(ModelRegistry.class);
		val node = node("foo", builder -> builder.withRegistry(modelRegistry));
		register(() -> NodeRegistration.of("bar", of(MyType.class))).execute(node);
		verify(modelRegistry, times(1)).register(ModelRegistration.of("foo.bar", MyType.class));
	}

	@Test
	void checkToString() {
		assertThat(register(NodeRegistration.of("bar", of(MyType.class))), hasToString("ModelActions.register(Suppliers.ofInstance(NodeRegistration(name=bar, components=[ModelProjections.managed(interface dev.nokee.model.internal.core.ModelActions_RegisterTest$MyType)], actionRegistrations=[])))"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(register(NodeRegistration.of("bar", of(MyType.class))), register(NodeRegistration.of("bar", of(MyType.class))))
			.addEqualityGroup(register(NodeRegistration.of("foo", of(MyType.class))))
			.addEqualityGroup(register(NodeRegistration.of("bar", of(MyOtherType.class))))
			.testEquals();
	}

	interface MyType {}
	interface MyOtherType {}
}
