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
package dev.nokee.utils.internal;

import org.gradle.api.Describable;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;

/**
 * Transformer-backed value source.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class ValueSourceTransformerAdapter implements ValueSource<Object, ValueSourceTransformerAdapter.Parameters>, Describable {
	public interface Parameters extends ValueSourceParameters {
		Property<Object> getInput();
		Property<String> getDisplayName();

		@SuppressWarnings("rawtypes")
		Property<Transformer> getTransformer();
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public Object obtain() {
		return getParameters().getTransformer().get().transform(getParameters().getInput().getOrNull());
	}

	@Override
	public String getDisplayName() {
		return getParameters().getDisplayName().getOrElse("type 'ValueSourceTransformerAdapter'");
	}
}
