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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.platform.base.internal.ComponentIdentifier;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

public class WarnUnbuildableLogger {
	private static final Logger LOGGER = Logging.getLogger(WarnUnbuildableLogger.class);
	private final ComponentIdentifier identifier;

	public WarnUnbuildableLogger(ComponentIdentifier identifier) {
		this.identifier = identifier;
	}

	public void warn() {
		LOGGER.warn(String.format("'%s' component in %s cannot build on this machine.", identifier.getName(), identifier.getProjectIdentifier()));
	}
}
