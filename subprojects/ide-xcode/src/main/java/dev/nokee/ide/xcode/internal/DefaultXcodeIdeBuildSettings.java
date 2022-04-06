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
package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildSettings;
import lombok.Getter;
import lombok.NonNull;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultXcodeIdeBuildSettings implements XcodeIdeBuildSettings {
	// Workaround for https://github.com/gradle/gradle/issues/13405
	@Getter private final List<Provider<? extends Object>> providers = new ArrayList<>();
	public abstract MapProperty<String, Object> getElements();

	@Override
	public XcodeIdeBuildSettings put(@NonNull String name, @NonNull Provider<? extends Object> value) {
		providers.add(value);
		getElements().put(name, value);
		return this;
	}

	@Override
	public XcodeIdeBuildSettings put(@NonNull String name, @NonNull Object value) {
		getElements().put(name, value);
		return this;
	}
}
