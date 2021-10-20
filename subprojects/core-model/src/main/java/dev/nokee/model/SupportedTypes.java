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
package dev.nokee.model;

import lombok.EqualsAndHashCode;

final class SupportedTypes {
	private SupportedTypes() {}

	public static SupportedType instanceOf(Class<?> type) {
		return new InstanceOfSupportedType(type);
	}

	@EqualsAndHashCode
	private static final class InstanceOfSupportedType implements SupportedType {
		private final Class<?> type;

		private InstanceOfSupportedType(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean supports(Class<?> type) {
			return this.type.equals(type);
		}

		@Override
		public String toString() {
			return type.getSimpleName();
		}
	}

	public static SupportedType anySubtypeOf(Class<?> type) {
		return new SubtypeOfSupportedType(type);
	}

	@EqualsAndHashCode
	private static final class SubtypeOfSupportedType implements SupportedType {
		private final Class<?> type;

		private SubtypeOfSupportedType(Class<?> type) {
			this.type = type;
		}

		@Override
		public boolean supports(Class<?> type) {
			return this.type.isAssignableFrom(type);
		}

		@Override
		public String toString() {
			return "subtypes of '" + type.getSimpleName() + "'";
		}
	}
}
