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
package dev.nokee.xcode.project.coders;

import java.util.List;
import java.util.Map;

public abstract class CoderType<T> {
	public static <T> CoderType<? extends T> anyOf(Class<T> baseType) {
		return new CoderAnyOfType<>(baseType);
	}

	public static CoderType<Boolean> trueFalseBoolean() {
		return new CoderTrueFalseBooleanType();
	}

	public static CoderType<Boolean> oneZeroBoolean() {
		return new CoderOneZeroBooleanType();
	}

	public static CoderType<Boolean> yesNoBoolean() {
		return new CoderYesNoBooleanType();
	}

	public static <T> CoderType<T> of(Class<T> type) {
		return new CoderOfType<>(type);
	}

	public static <T> CoderType<List<T>> list(CoderType<T> elementType) {
		return new CoderListType<>(elementType);
	}

	public static <T> CoderType<T> byRef(CoderType<T> objectType) {
		return new CoderByRefType<>(objectType);
	}

	public static <T> CoderType<T> byCopy(CoderType<T> objectType) {
		return new CoderByCopyType<>(objectType);
	}

	public static CoderType<Map<String, ?>> dict() {
		return new CoderDictionaryType();
	}

	public static CoderType<String> string() {
		return new CoderOfType<>(String.class);
	}

	public static CoderType<Integer> integer() {
		return new CoderOfType<>(Integer.class);
	}
}
