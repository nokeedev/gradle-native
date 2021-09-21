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
package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelProjection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class TypeCompatibilityModelProjectionSupportTest {
	protected abstract ModelProjection createSubject(Class<?> type);

	@Test
	void canQueryExactProjectionType() {
		assertTrue(createSubject(MyType.class).canBeViewedAs(of(MyType.class)),
			"projection should be viewable as exact type");
	}

	@Test
	void canQueryBaseProjectionType() {
		assertTrue(createSubject(MyType.class).canBeViewedAs(of(BaseType.class)),
			"projection should be viewable as base type");
		assertTrue(createSubject(MyType.class).canBeViewedAs(of(Object.class)),
			"projection should be viewable as base type");
	}

	@Test
	void cannotQueryWrongProjectionType() {
		assertFalse(createSubject(MyType.class).canBeViewedAs(of(WrongType.class)),
			"projection should not be viewable for wrong type");
	}

	@Test
	@DisabledIf("isInstanceModelProjection")
	void canDescribeBaseProjectionType() {
		assertThat(createSubject(BaseType.class).getTypeDescriptions(),
			contains("interface dev.nokee.model.internal.registry.TypeCompatibilityModelProjectionSupportTest$BaseType"));
	}

	@Test
	void canDescribeSubProjectionTypeImplementingInterface() {
		assertThat(createSubject(MyType.class).getTypeDescriptions(),
			contains("class dev.nokee.model.internal.registry.TypeCompatibilityModelProjectionSupportTest$MyType (or assignment compatible type thereof)"));
	}

	@Test
	void canDescribeSubProjectionTypeExtendingClass() {
		assertThat(createSubject(MySubType.class).getTypeDescriptions(),
			contains("class dev.nokee.model.internal.registry.TypeCompatibilityModelProjectionSupportTest$MySubType (or assignment compatible type thereof)"));
	}

	protected boolean isInstanceModelProjection() {
		return this.getClass().getSimpleName().contains("UnmanagedInstance");
	}

	interface BaseType {}
	static class MyType implements BaseType {}
	static class MySubType extends MyType {}
	interface WrongType {}
}
