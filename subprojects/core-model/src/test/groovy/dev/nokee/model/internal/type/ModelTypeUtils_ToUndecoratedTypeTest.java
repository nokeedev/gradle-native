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
package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.type.ModelTypeUtils.toUndecoratedType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ModelTypeUtils_ToUndecoratedTypeTest {
	@Test
	void undecorateTypeFromInstanceCreatedViaObjectFactoryOfClass() {
		assertThat(toUndecoratedType(objectFactory().newInstance(MyType.class).getClass()), is(MyType.class));
	}

	@Test
	void undecorateTypeFromInstanceCreatedViaObjectFactoryOfInterface() {
		assertThat(toUndecoratedType(objectFactory().newInstance(IMyType.class).getClass()), is(IMyType.class));
	}

	@Test
	void returnsSameTypeForDirectClassInstantiation() {
		assertThat(toUndecoratedType(new MyType().getClass()), is(MyType.class));
	}

	public interface IMyType {}
	public static class MyType {}
}
