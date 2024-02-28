/*
 * Copyright 2024 the original author or authors.
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

package com.example.greeter;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class GreeterTest {
	@Test
	public void testGreeter() {
		Greeter greeter = new Greeter();
		String greeting = greeter.sayHello("World");
		assertThat(greeting, equalTo("Bonjour, World!"));
	}

	@Test
	public void testNullGreeter() {
		Greeter greeter = new Greeter();
		String greeting = greeter.sayHello(null);
		assertThat(greeting, equalTo("name cannot be null"));
	}
}
