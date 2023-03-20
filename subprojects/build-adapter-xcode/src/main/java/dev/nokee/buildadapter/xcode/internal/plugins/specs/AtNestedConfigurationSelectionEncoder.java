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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode
public final class AtNestedConfigurationSelectionEncoder<T extends XCBuildConfiguration & Encodeable> implements ValueEncoder<XCBuildSpec, XCConfigurationList> {
	private final ValueEncoder<XCBuildSpec, T> delegate;

	public AtNestedConfigurationSelectionEncoder(ValueEncoder<XCBuildSpec, T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public XCBuildSpec encode(XCConfigurationList value, Context context) {
		final ImmutableMap.Builder<String, XCBuildSpec> specs = ImmutableMap.builder();
		for (XCBuildConfiguration buildConfiguration : value.getBuildConfigurations()) {
			@SuppressWarnings("unchecked")
			final T encodableBuildConfiguration = (T) buildConfiguration;
			specs.put(buildConfiguration.getName(), delegate.encode(encodableBuildConfiguration, context));
		}
		return new Spec(specs.build());
	}

	private static final class Spec implements XCBuildSpec {
		private final Map<String, XCBuildSpec> specs;

		public Spec(Map<String, XCBuildSpec> specs) {
			this.specs = specs;
		}

		@Override
		public XCBuildPlan resolve(ResolveContext context) {
			return specs.get(context.getConfiguration()).resolve(context);
		}
	}
}
