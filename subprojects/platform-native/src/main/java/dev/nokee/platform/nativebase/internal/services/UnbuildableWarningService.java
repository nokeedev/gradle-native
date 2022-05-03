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
package dev.nokee.platform.nativebase.internal.services;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.nativebase.internal.rules.WarnUnbuildableLogger;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import java.util.HashSet;
import java.util.Set;

public abstract class UnbuildableWarningService implements BuildService<BuildServiceParameters.None> {
	private static final Logger LOGGER = Logging.getLogger(WarnUnbuildableLogger.class);
	private final Set<ComponentIdentifier> identifiers = new HashSet<>();
	private final Object lock = new Object();

	public void warn(ComponentIdentifier identifier) {
		if (!identifiers.contains(identifier)) {
			synchronized (lock) {
				if (identifiers.add(identifier)) {
					LOGGER.warn(String.format("'%s' component in %s cannot build on this machine.", identifier.getName(), identifier.getProjectIdentifier()));
				}
			}
		}
	}
}
