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
package dev.nokee.xcode.objects.targets;

import dev.nokee.xcode.objects.configuration.XCConfigurationList;

import java.util.function.Consumer;

public interface BuildConfigurationsAwareBuilder<SELF> {
	SELF buildConfigurations(XCConfigurationList buildConfigurations);

	default SELF buildConfigurations(Consumer<? super XCConfigurationList.Builder> action) {
		final XCConfigurationList.Builder builder = XCConfigurationList.builder();
		action.accept(builder);
		return buildConfigurations(builder.build());
	}
}
