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
package dev.gradleplugins.runnerkit.providers;

import dev.nokee.platform.cpp.BuildExperimentInvocationSpec;

public final class InvocationSpecProvider<I extends BuildExperimentInvocationSpec> extends AbstractGradleExecutionProvider<I> {
	@SuppressWarnings("unchecked")
	public static <I extends BuildExperimentInvocationSpec> InvocationSpecProvider<I> unset() {
		return noValue(InvocationSpecProvider.class);
	}

	@SuppressWarnings("unchecked")
	public static <I extends BuildExperimentInvocationSpec> InvocationSpecProvider<I> of(I spec) {
		return fixed(InvocationSpecProvider.class, spec);
	}
}
