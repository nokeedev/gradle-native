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
package dev.nokee.platform.base.internal;

import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.specs.Spec;

import java.util.Objects;

@EqualsAndHashCode
public final class SpecFilteringAction<T> implements Action<T> {
	private final Spec<? super T> spec;
	private final Action<? super T> action;

	SpecFilteringAction(Spec<? super T> spec, Action<? super T> action) {
		this.spec = Objects.requireNonNull(spec);
		this.action = Objects.requireNonNull(action);
	}

	@Override
	public void execute(T t) {
		if (spec.isSatisfiedBy(t)) {
			action.execute(t);
		}
	}
}
