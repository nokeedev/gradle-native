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

public interface WellBehavingTaskPropertyMutator {
	String configureWith(WellBehavingTaskPropertyValue value);

	@Value
	class Property implements WellBehavingTaskPropertyMutator {
		String propertyName;

		@Override
		public String configureWith(WellBehavingTaskPropertyValue value) {
			return propertyName + " = " + value.toString();
		}
	}

	@Value
	class FileCollection implements WellBehavingTaskPropertyMutator {
		String propertyName;

		@Override
		public String configureWith(WellBehavingTaskPropertyValue value) {
			return propertyName + ".from(" + value.toString() + ")";
		}
	}

}
