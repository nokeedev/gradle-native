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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.project.ValueEncoder;
import lombok.EqualsAndHashCode;
import org.gradle.api.tasks.InputFiles;

@EqualsAndHashCode
public final class AtInputFilesEncoder<IN> implements ValueEncoder<XCBuildSpec, IN> {
	private final ValueEncoder<XCBuildSpec, IN> delegate;

	public AtInputFilesEncoder(ValueEncoder<XCBuildSpec, IN> delegate) {
		this.delegate = delegate;
	}

	@Override
	public XCBuildSpec encode(IN value, Context context) {
		final XCBuildSpec references = delegate.encode(value, context);
		return new XCBuildSpec() {
			@Override
			public XCBuildPlan resolve(ResolveContext context) {
				final XCBuildPlan result = references.resolve(context);
				return new XCBuildPlan() {
					@InputFiles
					public Object getValue() {
						return result;
					}

					@Override
					public String toString() {
						return result.toString();
					}
				};
			}
		};
	}
}
