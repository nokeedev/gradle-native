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

import dev.nokee.model.internal.registry.DefaultModelRegistry;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModelPropertiesTest {
	private final DefaultModelRegistry registry = new DefaultModelRegistry(objectFactory()::newInstance);
	private final ModelNode subject = registry.get(ModelPath.root());
	private final ModelNode propertyA = ModelNodes.of(registry.register(ModelRegistration.builder()
		.withComponent(tag(ModelPropertyTag.class))
		.withComponent(new ModelPropertyTypeComponent(of(MyType.class)))
		.withComponent(new GradlePropertyComponent(objectFactory().property(MyType.class).value(new MyType("valueA"))))
		.withComponent(new ModelPathComponent(ModelPath.path("propA")))
		.build()));
	private final ModelNode propertyB = ModelNodes.of(registry.register(ModelRegistration.builder()
		.withComponent(tag(ModelPropertyTag.class))
		.withComponent(new ModelPropertyTypeComponent(of(MyType.class)))
		.withComponent(new GradlePropertyComponent(objectFactory().property(MyType.class).value(new MyType("valueB"))))
		.withComponent(new ModelPathComponent(ModelPath.path("propB")))
		.build()));
	private final ModelNode notProperty = ModelNodes.of(registry.register(ModelRegistration.builder()
		.withComponent(new ModelPathComponent(ModelPath.path("propC")))
		.withComponent(ofInstance(new MyType("valueC")))
		.build()));

	@Test
	void canRetrieveProperty() {
		val property = assertDoesNotThrow(() -> ModelProperties.getProperty(subject, "propA"));
		assertThat(property.as(MyType.class).map(MyType::getValue), providerOf("valueA"));
	}

	@Test
	void throwsExceptionIfEntityIsNotProperty() {
		assertThrows(RuntimeException.class, () -> ModelProperties.getProperty(subject, "propC"));
	}

	@Test
	void throwsExceptionIfNoEntityMatchPropertyName() {
		assertThrows(RuntimeException.class, () -> ModelProperties.getProperty(subject, "propNonExistent"));
	}

	@Test
	void canRetrieveProperties() {
		assertThat(ModelProperties.getProperties(subject).map(ModelElement::getName).collect(Collectors.toList()), contains("propA", "propB"));
	}

	@Value
	private static class MyType {
		String value;
	}
}
