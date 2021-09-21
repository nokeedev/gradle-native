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
package dev.nokee.fixtures;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionTestFixture {
	public static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		assertTrue("collection needs to have one element, was empty", iterator.hasNext());
		T result = iterator.next();
		assertFalse("collection needs to only have one element, more than one element found", iterator.hasNext());
		return result;
	}
}
