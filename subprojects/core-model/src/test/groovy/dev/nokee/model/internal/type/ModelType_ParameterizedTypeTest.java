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

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ModelType_ParameterizedTypeTest {
	@Test
	void nonParameterizedType() {
		assertThat(of(MyType.class).isParameterized(), equalTo(false));
	}

	@Test
	void nonParameterizedTypeHasNoTypeVariables() {
		assertThat(of(MyType.class).getTypeVariables(), empty());
	}

	@Test
	void parameterizedType() {
		assertThat(of(new TypeOf<MyGenericType<String>>() {}).isParameterized(), equalTo(true));
	}

	@Test
	void parameterizedTypeHasTypeVariables() {
		assertThat(of(new TypeOf<MyGenericType<String>>() {}).getTypeVariables(), contains(of(String.class)));
	}

	interface MyType {}
	interface MyGenericType<T> {}
}
