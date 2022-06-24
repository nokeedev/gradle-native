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
package dev.nokee.model.internal;

import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.model.internal.tags.ModelTags;
import lombok.val;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.DomainObjectEntities.newEntity;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

class DomainObjectEntitiesTagTest {
	@Test
	void hasCustomTagDirectlyOnType() {
		val subject = newEntity("my-name", MyTypeSpec.class).build();
		assertThat(subject.getComponents(), hasTag(MyTypeSpec.Tag.class));
	}

	@Test
	void hasCustomTagInheritedFromInterface() {
		val subject = newEntity("my-name", MyOtherTypeSpec.class).build();
		assertThat(subject.getComponents(), allOf(hasTag(MyOtherTypeSpec.Tag.class),  hasTag(MyTypeSpec.Tag.class)));
	}

	@Test
	void hasCustomTagInheritedFromSuperClass() {
		val subject = newEntity("my-name", ConcreteTypeSpec.class).build();
		assertThat(subject.getComponents(), hasTag(AbstractTypeSpec.Tag.class));
	}

	private static Matcher<Iterable<? super ModelComponent>> hasTag(Class<? extends ModelTag> tag) {
		return hasItem(new FeatureMatcher<ModelComponent, ModelComponentType<?>>(equalTo(ModelTags.typeOf(tag)), "", "") {
			@Override
			protected ModelComponentType<?> featureValueOf(ModelComponent actual) {
				return actual.getComponentType();
			}
		});
	}

	@DomainObjectEntities.Tag(MyTypeSpec.Tag.class)
	interface MyTypeSpec {
		interface Tag extends ModelTag {}
	}

	@DomainObjectEntities.Tag(MyOtherTypeSpec.Tag.class)
	interface MyOtherTypeSpec extends MyTypeSpec {
		interface Tag extends ModelTag {}
	}

	@DomainObjectEntities.Tag(AbstractTypeSpec.Tag.class)
	static abstract class AbstractTypeSpec {
		interface Tag extends ModelTag {}
	}

	static /*final*/ class ConcreteTypeSpec extends AbstractTypeSpec {}
}
