/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.platform.base.internal.rules;

import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.HasDevelopmentVariant;
import org.gradle.api.Action;
import org.gradle.api.provider.ProviderFactory;

public final class DevelopmentBinaryConventionRule implements Action<HasDevelopmentBinary> {
	private final ProviderFactory providers;

	public DevelopmentBinaryConventionRule(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void execute(HasDevelopmentBinary component) {
		component.getDevelopmentBinary().convention(providers.provider(() -> {
			if (component instanceof HasDevelopmentVariant) {
				return ((HasDevelopmentVariant<?>) component).getDevelopmentVariant()
					.flatMap(HasDevelopmentBinary::getDevelopmentBinary);
			} else {
				return null;
			}
		}).flatMap(it -> it));
	}
}
