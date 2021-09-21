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
package dev.nokee.ide.visualstudio;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VisualStudioIdeConfiguration {
	String identifier;

	/**
	 * Returns the configuration identifier.
	 *
	 * @return a {@link String} instance of the identifier, never null.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Creates a configuration of the specified identifier.
	 *
	 * @param identifier a identifier for the configuration to create.
	 * @return a {@link VisualStudioIdeConfiguration} instance, never null.
	 */
	public static VisualStudioIdeConfiguration of(String identifier) {
		return new VisualStudioIdeConfiguration(identifier);
	}

	@Override
	public String toString() {
		return identifier;
	}
}
