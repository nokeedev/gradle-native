/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.model.internal.discover;

import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Example implementation for AbstractVariantAwareComponentFunctionalTest
//   We will make this easier in future releases.
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@DiscoverableAction
@DiscoverRule(ProvideConfigurations.Rule.class)
public @interface ProvideConfigurations {
	final class Rule implements DisRule {
		public void execute(Details details) {
			details.newCandidate(ElementName.of("*"), ModelType.of(Configuration.class));
		}
	}

	@ProvideConfigurations
	final class Action<T> implements org.gradle.api.Action<T> {
		private final org.gradle.api.Action<T> delegate;

		public Action(org.gradle.api.Action<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public void execute(T t) {
			this.delegate.execute(t);
		}
	}
}
