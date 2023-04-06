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

import java.util.Map;

public interface XCBuildSettingLayer {
	XCBuildSetting find(SearchContext context);

	Map<String, XCBuildSetting> findAll();

	interface SearchContext {
		String getName();

		/**
		 * Continue the search in the next layer.
		 *
		 * @return the build settings found in lower layers
		 */
		default XCBuildSetting findNext() {
			throw new UnsupportedOperationException();
		}
	}

	void accept(Visitor visitor);

	interface Visitor {
		void visit(XCBuildSettingLayer layer);
	}
}
