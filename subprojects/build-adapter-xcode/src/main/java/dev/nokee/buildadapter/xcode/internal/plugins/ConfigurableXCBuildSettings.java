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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import dev.nokee.utils.DeferredUtils;
import dev.nokee.xcode.CompositeXCBuildSettingLayer;
import dev.nokee.xcode.DefaultXCBuildSetting;
import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.DefaultXCBuildSettings;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettings;
import dev.nokee.xcode.XCString;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public abstract class ConfigurableXCBuildSettings implements XCBuildSettings {
	public ConfigurableXCBuildSettings() {
		getValue().set(getObjects().map(it -> {
			List<Object> l = new ArrayList<>(it);
			Collections.reverse(l);
			// flatUnpack takes care of the Iterable, List, Set, Provider of any value, Callable of any value, etc.
			//   toLayer only needs to consider Map, XCBuildSettingLayer
			return new DefaultXCBuildSettings(new CompositeXCBuildSettingLayer(DeferredUtils.flatUnpack(l).stream().map(ConfigurableXCBuildSettings::toLayer).collect(Collectors.toList())));
		}));
	}

	@Nullable
	@Override
	public String get(String name) {
		return getValue().get().get(name);
	}

	/**
	 * This method converts the supplied build settings based on its type:
	 *
	 * <ul>
	 *
	 * <li>A {@link Map} of build setting name to value which interpret any build setting reference. Any {@link Iterable} value will be converted into space separated build setting value.</li>
	 *
	 * <li>A {@link XCBuildSettingLayer}</li>
	 *
	 * <li>A {@link Provider} of any supported type. The provider's value is resolved recursively.</li>
	 *
	 * <li>A Groovy {@link Closure} or Kotlin function that returns any supported type. The closure's return value is resolved recursively.</li>
	 *
	 * <li>A {@link java.util.concurrent.Callable} that returns any supported type. The callable's return value is resolved recursively.</li>
	 *
	 * </ul>
	 *
	 * @param first  the first value
	 * @param other  the other values
	 * @return this
	 */
	public ConfigurableXCBuildSettings from(Object first, Object... other) {
		getObjects().add(first);
		getObjects().addAll(other);
		return this;
	}

	/**
	 * Set the build settings of this instance. See {@link #from(Object, Object...)} for how the build settings are evaluated.
	 *
	 * @param first  the first value
	 * @param other  the other values
	 */
	public void setFrom(Object first, Object... other) {
		getObjects().empty();
		getObjects().add(first);
		getObjects().addAll(other);
	}

	private static XCBuildSettingLayer toLayer(Object obj) {
		if (obj instanceof Map) {
			return new DefaultXCBuildSettingLayer(((Map<?, ?>) obj).entrySet().stream().map(it -> {
				if (it.getValue() instanceof Iterable) {
					val str = Streams.stream((Iterable<?>) it.getValue()).map(Object::toString).collect(joining(" "));
					return new DefaultXCBuildSetting(it.getKey().toString(), XCString.of(str));
				} else {
					return new DefaultXCBuildSetting(it.getKey().toString(), XCString.of(it.getValue().toString()));
				}
			}).collect(ImmutableMap.toImmutableMap(XCBuildSetting::getName, Function.identity())));
		} else if (obj instanceof XCBuildSettingLayer) {
			return (XCBuildSettingLayer) obj;
		} else {
			throw new IllegalArgumentException(obj.toString());
		}
	}

	// TODO: Use ProviderConvertible
	public Provider<XCBuildSettings> asProvider() {
		return getValue();
	}

	public ConfigurableXCBuildSettings disallowChanges() {
		getObjects().disallowChanges();
		return this;
	}

	@Internal
	protected abstract ListProperty<Object> getObjects();

	@Input
	protected abstract Property<XCBuildSettings> getValue();
}
