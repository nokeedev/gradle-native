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
package dev.nokee.xcode;

import javax.annotation.Nullable;

public final class DefaultXCBuildSetting implements XCBuildSetting {
	private final String name;
	private final XCString value;

	public DefaultXCBuildSetting(String name, XCString value) {
		assert name != null : "'name' must not be null";
		assert value != null : "'value' must not be null";
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String evaluate(EvaluationContext context) {
		return value.resolve(new EvaluationAwareResolveContext(context));
	}

	@Override
	public String toString() {
		return value.toString(); // not perfect, but good enough for now - see XcodeTargetExecTask
	}

	private static final class EvaluationAwareResolveContext implements XCString.ResolveContext {
		private final EvaluationContext context;

		private EvaluationAwareResolveContext(EvaluationContext context) {
			assert context != null : "'context' must not be null";
			this.context = context;
		}

		@Nullable
		@Override
		public String get(String variableName) {
			return context.get(variableName);
		}
	}
}
