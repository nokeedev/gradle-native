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
package dev.nokee.fixtures.tasks;

import lombok.Value;

public interface WellBehavingTaskPropertyValue {

	@Value(staticConstructor = "of")
	class File implements WellBehavingTaskPropertyValue {
		String value;

		@Override
		public String toString() {
			return "file('" + value + "')";
		}
	}

	@Value(staticConstructor = "of")
	class Quoted implements WellBehavingTaskPropertyValue {
		String value;

		@Override
		public String toString() {
			return "'" + value + "'";
		}
	}

	@Value(staticConstructor = "of")
	class GroovyDslExpression implements WellBehavingTaskPropertyValue {
		String value;

		@Override
		public String toString() {
			return value;
		}
	}
}
