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
package dev.nokee.model.internal.registry


import dev.nokee.model.internal.core.*
import org.junit.jupiter.api.Test

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory
import static dev.nokee.model.internal.core.ModelProjections.ofInstance
import static org.junit.jupiter.api.Assertions.assertThrows

class ModelBackedModelElementGroovyDslTest {
	private final DefaultModelRegistry registry = new DefaultModelRegistry({ type, parameters -> objectFactory().newInstance(type, parameters)})
	private final ModelNode node = ModelNodes.of(registry.register(ModelRegistration.builder().withComponent(ofInstance(new MyType())).withComponent(ModelPath.path("zusi")).build()));
	private final ModelElement subject = new ModelNodeBackedElement(node);

	@Test
	void throwsExceptionWhenTypeCastingIntoInstanceOfType() {
		assertThrows(UnsupportedOperationException.class, { subject as MyType })
	}

	private static final class MyType {}
}
