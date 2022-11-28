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
package dev.nokee.utils;

import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.internal.provider.PropertyInternal;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.BiConsumer;

public final class ConfigureUtils {
	private ConfigureUtils() {}

	/**
	 * Set property value to the specified value.
	 *
	 * @param self the property to configure.
	 * @param value the value to set.
	 * @param <T> the type of the property.
	 */
	public static <T> void setPropertyValue(Property<T> self, Object value) {
		((PropertyInternal<?>) self).setFromAnyValue(value);
	}

	/**
	 * Set property value to the specified value.
	 *
	 * @param self the set property to configure.
	 * @param value the value to set.
	 * @param <T> the type of the property.
	 */
	public static <T> void setPropertyValue(SetProperty<T> self, Object value) {
		((PropertyInternal<?>) self).setFromAnyValue(value);
	}

	/**
	 * Set property value to the specified value.
	 *
	 * @param self the list property to configure.
	 * @param value the value to set.
	 * @param <T> the type of the property.
	 */
	public static <T> void setPropertyValue(ListProperty<T> self, Object value) {
		((PropertyInternal<?>) self).setFromAnyValue(value);
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param displayName  the display name to use, must not be null
	 * @param <T> the type of the property.
	 * @return self
	 */
	public static <T> Property<T> configureDisplayName(Property<T> self, String displayName) {
		attachDisplayName(self, Describables.of(displayName));
		return self;
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param displayName  the display name to use, must not be null
	 * @return self
	 */
	public static RegularFileProperty configureDisplayName(RegularFileProperty self, String displayName) {
		attachDisplayName(self, Describables.of(displayName));
		return self;
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param displayName  the display name to use, must not be null
	 * @return self
	 */
	public static DirectoryProperty configureDisplayName(DirectoryProperty self, String displayName) {
		attachDisplayName(self, Describables.of(displayName));
		return self;
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param displayName the display name to use, must not be null
	 * @param <T> the type of the property.
	 * @return self
	 */
	public static <T> SetProperty<T> configureDisplayName(SetProperty<T> self, String displayName) {
		attachDisplayName(self, Describables.of(displayName));
		return self;
	}

	/**
	 * Returns the default display name for the specified property name.
	 *
	 * @param propertyName  the property name for the display name, must not be null
	 * @return the default display name for the specified property name, never null
	 */
	public static String property(String propertyName) {
		return "property '" + propertyName + "'";
	}

	/**
	 * Configures the property display name with the specified value.
	 *
	 * @param self the property to configure.
	 * @param displayName  the display name to use, must not be null
	 * @param <T> the type of the property.
	 * @return self
	 */
	public static <T> ListProperty<T> configureDisplayName(ListProperty<T> self, String displayName) {
		attachDisplayName(self, Describables.of(displayName));
		return self;
	}

	private static BiConsumer<Object, DisplayName> attachDisplayName;
	private static BiConsumer<Object, DisplayName> ATTACH_DISPLAY_NAME_IDENTITY = (obj, arg) -> {};
	private static void attachDisplayName(Object obj, DisplayName displayName) {
		if (attachDisplayName == null) {
			attachDisplayName = findAttachOwnerMethod().orElseGet(() -> findAttachDisplayNameMethod().orElse(ATTACH_DISPLAY_NAME_IDENTITY));
		}
		attachDisplayName.accept(obj, displayName);
	}

	private static Optional<BiConsumer<Object, DisplayName>> findAttachDisplayNameMethod() {
		try {
			val OwnerAware = Class.forName("org.gradle.api.internal.provider.OwnerAware");
			val method = OwnerAware.getMethod("attachDisplayName", DisplayName.class);
			return Optional.of((obj, arg) -> {
				try {
					method.invoke(obj, arg);
				} catch (IllegalAccessException | InvocationTargetException e) {
					// Something is wrong so let's ignore any more display name configuration
					attachDisplayName = ATTACH_DISPLAY_NAME_IDENTITY;
				}
			});
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	private static Optional<BiConsumer<Object, DisplayName>> findAttachOwnerMethod() {
		try {
			val OwnerAware = Class.forName("org.gradle.internal.state.OwnerAware");
			val ModelObject = Class.forName("org.gradle.internal.state.ModelObject");
			val method = OwnerAware.getMethod("attachOwner", ModelObject, DisplayName.class);
			return Optional.of((obj, arg) -> {
				try {
					method.invoke(obj, null, arg);
				} catch (IllegalAccessException | InvocationTargetException e) {
					// Something is wrong so let's ignore any more display name configuration
					attachDisplayName = ATTACH_DISPLAY_NAME_IDENTITY;
				}
			});
		} catch (NoSuchMethodException | ClassNotFoundException e) {
			return Optional.empty();
		}
	}

	/**
	 * Creates an action that uses the given closure to configure objects of type T.
	 */
	public static <T> Action<T> configureUsing(@Nullable @SuppressWarnings("rawtypes") Closure configureClosure) {
		if (configureClosure == null) {
			return ActionUtils.doNothing();
		} else {
			return new ClosureWrappedConfigureAction<>(configureClosure);
		}
	}
}
