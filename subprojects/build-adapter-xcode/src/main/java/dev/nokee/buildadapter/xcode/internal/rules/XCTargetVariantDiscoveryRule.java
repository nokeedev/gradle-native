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
package dev.nokee.buildadapter.xcode.internal.rules;

import com.google.common.collect.Streams;
import dev.nokee.buildadapter.xcode.internal.XcodeConfigurationParameter;
import dev.nokee.buildadapter.xcode.internal.plugins.XCProjectAdapterSpec;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCTargetReference;
import lombok.val;
import org.gradle.api.Action;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class XCTargetVariantDiscoveryRule implements Action<XCProjectAdapterSpec> {
	private final XCLoader<Iterable<String>, XCTargetReference> loader;
	private final XcodeConfigurationParameter configurationParameter;

	public XCTargetVariantDiscoveryRule(XCLoader<Iterable<String>, XCTargetReference> loader, XcodeConfigurationParameter configurationParameter) {
		this.loader = loader;
		this.configurationParameter = configurationParameter;
	}

	@Override
	public void execute(XCProjectAdapterSpec component) {
		val requestedConfiguration = configurationParameter.get();
		component.getConfigurations().convention(component.getTarget().map(it -> Streams.stream(it.load(loader)).flatMap(configuration -> {
			if (requestedConfiguration == null || configuration.equals(requestedConfiguration)) {
				return Stream.of(configuration);
			} else {
				return Stream.empty();
			}
		}).collect(Collectors.toList())));
	}
}
