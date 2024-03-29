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
package dev.nokee.nvm;

import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

import java.lang.reflect.InvocationTargetException;

final class ProviderUtils {
	private ProviderUtils() {}

	public static <T> Provider<T> forUseAtConfigurationTime(Provider<T> self) {
		if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0 && GradleVersion.current().compareTo(GradleVersion.version("7.4")) < 0) {
			try {
				@SuppressWarnings("unchecked")
				final Provider<T> result = (Provider<T>) Provider.class.getMethod("forUseAtConfigurationTime").invoke(self);
				return result;
			} catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		}
		return self;
	}
}
