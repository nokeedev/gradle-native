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

import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(ModelLookupEmptyResult.class)
class ModelLookupEmptyResultTest {
	private final ModelLookup.Result subject = ModelLookupEmptyResult.INSTANCE;

	@Test
	void returnsAnEmptyListWhenGettingTheValues() {
		assertThat(subject.get(), empty());
	}

	@Test
	void returnsAnEmptyListWhenMappingTheResult() {
		assertThat(subject.map(Function.identity()), empty());
	}

	@Test
	void canCreateEmptyResultUsingFactoryMethod() {
		assertThat(ModelLookup.Result.empty(), equalTo(subject));
	}

	@Test
	void hasNoValuesToIterateOver() {
		assertDoesNotThrow(() -> subject.forEach(t -> { throw new AssertionError("Not expecting this exception"); }));
	}
}
