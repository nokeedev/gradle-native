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
package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.*;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ModelBackedModelElementTest implements ModelElementTester {
	private final DefaultModelRegistry registry = new DefaultModelRegistry(objectFactory()::newInstance);
	private final ModelNode node = ModelNodes.of(registry.register(ModelRegistration.builder().withComponent(ofInstance(new MyType())).withComponent(ModelPath.path("zusi")).build()));
	private final ModelElement subject = new ModelNodeBackedElement(node);

	@Override
	public ModelElement subject() {
		return subject;
	}

	@Test
	void canCastElement() {
		assertThat(subject.as(MyType.class).getType(), is(MyType.class));
	}

	@Test
	void useInstanceTypeInsteadOfCastType() {
		assertThat(subject.as(IMyType.class).getType(), is(MyType.class));
	}

	private interface IMyType {}
	private static final class MyType implements IMyType {}
}
