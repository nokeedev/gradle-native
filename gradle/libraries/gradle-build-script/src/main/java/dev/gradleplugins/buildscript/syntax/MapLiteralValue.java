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
package dev.gradleplugins.buildscript.syntax;

import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.function.BiConsumer;

@EqualsAndHashCode
final class MapLiteralValue implements Expression {
	private final Map<String, Expression> literal;

	public MapLiteralValue(Map<String, Expression> literal) {
		this.literal = literal;
	}

	boolean isEmpty() {
		return literal.isEmpty();
	}

	public void forEach(BiConsumer<? super String, ? super Expression> action) {
		literal.forEach(action);
	}
}
