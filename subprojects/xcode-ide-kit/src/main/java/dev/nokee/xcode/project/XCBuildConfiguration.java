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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

/**
 * Build configuration containing a file reference ton an xcconfig file and additional inline
 * settings.
 */
public final class XCBuildConfiguration extends PBXBuildStyle {
    public XCBuildConfiguration(String name) {
        super(name);
    }

	private XCBuildConfiguration(String name, Map<String, ?> buildSettings) {
		super(name, buildSettings);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private Map<String, ?> buildSettings = Collections.emptyMap();

		private Builder() {}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder buildSettings(Map<String, ?> buildSettings) {
			this.buildSettings = ImmutableMap.copyOf(buildSettings);
			return this;
		}

		public XCBuildConfiguration build() {
			return new XCBuildConfiguration(name, buildSettings);
		}
	}
}
