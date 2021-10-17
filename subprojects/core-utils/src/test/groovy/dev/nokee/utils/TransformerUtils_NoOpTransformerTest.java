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
package dev.nokee.utils;

import lombok.val;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class TransformerUtils_NoOpTransformerTest {
	@Test
	void alwaysReturnTheInputObject() {
		val obj1 = new Object();
		val obj2 = "obj";
		val obj3 = new Double(4.2);

		assertThat(noOpTransformer().transform(obj1), equalTo(obj1));
		assertThat(noOpTransformer().transform(obj2), equalTo(obj2));
		assertThat(noOpTransformer().transform(obj3), equalTo(obj3));
	}

	@Test
	void alwaysReturnTheSameInstance() {
		assertThat(noOpTransformer(), equalTo(noOpTransformer()));
	}

	@Test
	void checkToString() {
		assertThat(noOpTransformer(), hasToString("TransformerUtils.noOpTransformer()"));
	}

	@Test
	void canUseNoOpTransformerWhenOutputTypeIsASuperTypeOfTheInputType() {
		// compile-time assertion
		inputTypeToSuperType(noOpTransformer());
	}

	@Test
	void cannotUseNoOpTransformerWhenOutputTypeIsAChildTypeOfTheInputType() {
		// compile-time assertion, uncomment to test -> it should not compile
//		inputTypeToChildType(noOpTransformer());
	}

	@Test
	void canUseNoOpTransformerWhenOutputTypeIsTheSameAsInputType() {
		// compile-time assertion, uncomment to test -> it should not compile
		bothInputAndOutputTypeAreTheSame(noOpTransformer());
	}

	private static void inputTypeToSuperType(Transformer<Base, Child> transformer) {}
	private static void inputTypeToChildType(Transformer<Child, Base> transformer) {}
	private static void bothInputAndOutputTypeAreTheSame(Transformer<Base, Base> transformer) {}

	interface Base {}
	interface Child extends Base {}
}
