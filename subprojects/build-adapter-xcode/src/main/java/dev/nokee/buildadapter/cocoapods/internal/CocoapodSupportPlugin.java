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
package dev.nokee.buildadapter.cocoapods.internal;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.io.File;

import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;

abstract class CocoapodSupportPlugin implements Plugin<Settings> {
	private final CocoaPodsProviders providers;

	@Inject
	public CocoapodSupportPlugin(ProviderFactory providers) {
		this.providers = new CocoaPodsProviders(providers);
	}

	@Override
	public void apply(Settings settings) {
		forUseAtConfigurationTime(providers.installation(spec -> {
			spec.getPodfile().set(forUseAtConfigurationTime(providers.podfile(new File(settings.getSettingsDir(), "Podfile"))));
			spec.getCacheFile().set(new File(settings.getSettingsDir(), ".gradle/Podfile"));
		})).getOrNull();
	}
}
