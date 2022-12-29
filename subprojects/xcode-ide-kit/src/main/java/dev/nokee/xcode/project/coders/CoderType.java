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

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

public abstract class CoderType<T> {
	public static <T> CoderType<? extends T> anyOf(Class<T> baseType) {
		return new AnyOfType<>(baseType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class AnyOfType<T> extends CoderType<T> {
		private final Class<T> baseType;

		public AnyOfType(Class<T> baseType) {
			this.baseType = baseType;
		}

		@Override
		public String toString() {
			return "? extends " + baseType.getCanonicalName();
		}
	}

	public static CoderType<Boolean> trueFalseBoolean() {
		return new BooleanType(BooleanType.Type.TRUE_FALSE);
	}

	public static CoderType<Boolean> oneZeroBoolean() {
		return new BooleanType(BooleanType.Type.ONE_ZERO);
	}

	public static CoderType<Boolean> yesNoBoolean() {
		return new BooleanType(BooleanType.Type.YES_NO);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class BooleanType extends CoderType<Boolean> {
		private final Type type;

		private BooleanType(Type type) {
			this.type = type;
		}

		enum Type { TRUE_FALSE, ONE_ZERO, YES_NO }
	}

	public static <T> CoderType<T> of(Class<T> type) {
		return new OfType<>(type);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class OfType<T> extends CoderType<T> {
		private final Class<T> type;

		public OfType(Class<T> type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type.getCanonicalName();
		}
	}

	public static <T> CoderType<List<T>> listOf(CoderType<T> elementType) {
		return new ListOfType<>(elementType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class ListOfType<E> extends CoderType<List<E>> {
		private final CoderType<E> elementType;

		public ListOfType(CoderType<E> elementType) {
			this.elementType = elementType;
		}

		@Override
		public String toString() {
			return "List<" + elementType + ">";
		}
	}

	public static <T> CoderType<T> byRef(CoderType<T> objectType) {
		return new ByRefType<>(objectType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class ByRefType<T> extends CoderType<T> {
		private final CoderType<T> objectType;

		public ByRefType(CoderType<T> objectType) {
			this.objectType = objectType;
		}

		@Override
		public String toString() {
			return "byRef " + objectType;
		}
	}

	public static <T> CoderType<T> byCopy(CoderType<T> objectType) {
		return new ByCopyType<>(objectType);
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class ByCopyType<T> extends CoderType<T> {
		private final CoderType<T> objectType;

		public ByCopyType(CoderType<T> objectType) {
			this.objectType = objectType;
		}

		@Override
		public String toString() {
			return "byCopy " + objectType;
		}
	}

	public static CoderType<Map<String, ?>> dict() {
		return new MapType();
	}

	@EqualsAndHashCode(callSuper = false)
	private static final class MapType extends CoderType<Map<String, ?>> {
		@Override
		public String toString() {
			return "Map<String, ?>";
		}
	}

	public static CoderType<String> string() {
		return new OfType<>(String.class);
	}

	public static CoderType<Integer> integer() {
		return new OfType<>(Integer.class);
	}
}
