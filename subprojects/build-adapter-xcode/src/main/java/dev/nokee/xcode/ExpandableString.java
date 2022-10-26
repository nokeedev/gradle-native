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
package dev.nokee.xcode;

import lombok.val;

import java.util.List;

public abstract class ExpandableString {
	public abstract String evaluate(EvaluateContext context);

	public static ExpandableString of(String value) {
		// TODO: Parse value to create a series of segment
		throw new UnsupportedOperationException();
	}

	// Ex: some value
	private static final class StringSegment extends ExpandableString {
		private final String value;

		private StringSegment(String value) {
			this.value = value;
		}

		@Override
		public String evaluate(EvaluateContext context) {
			return value;
		}
	}

	// Ex: $(...)
	private static abstract class BuildSettingSegment extends ExpandableString {
	}

	// Ex: $(BUILD_SETTING)
	private static abstract class SingleBuildSettingSegment extends BuildSettingSegment {
		private final String name;

		private SingleBuildSettingSegment(String name) {
			this.name = name;
		}

		@Override
		public String evaluate(EvaluateContext context) {
			return context.get(name);
		}
	}

	// Ex: $($(BUILD_SETTING))
	private static final class NestedBuildSettingSegment extends ExpandableString {
		private final BuildSettingSegment delegate;

		private NestedBuildSettingSegment(BuildSettingSegment delegate) {
			this.delegate = delegate;
		}


		@Override
		public String evaluate(EvaluateContext context) {
			return context.get(delegate.evaluate(context));
		}
	}

	private static final class CompositeBuildSettingSegment extends ExpandableString {
		private final List<ExpandableString> segments;

		private CompositeBuildSettingSegment(List<ExpandableString> segments) {
			this.segments = segments;
		}

		@Override
		public String evaluate(EvaluateContext context) {
			val builder = new StringBuilder();
			for (ExpandableString segment : segments) {
				builder.append(segment.evaluate(context));
			}
			return builder.toString();
		}
	}
}
