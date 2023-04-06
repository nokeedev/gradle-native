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

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@EqualsAndHashCode
public final class CompositeXCBuildSettingLayer implements XCBuildSettingLayer, Serializable {
	private final Iterable<XCBuildSettingLayer> layers;

	public CompositeXCBuildSettingLayer(Iterable<XCBuildSettingLayer> layers) {
		assert layers != null : "'layers' must not be null";
		this.layers = layers;
	}

	@Override
	public XCBuildSetting find(SearchContext context) {
		assert context != null : "'context' must not be null";
		return new LayeredSearchContext(context, layers.iterator()).findNext();
	}

	private static final class LayeredSearchContext implements SearchContext {
		private final SearchContext parent;
		private final Iterator<XCBuildSettingLayer> layers;

		private LayeredSearchContext(SearchContext parent, Iterator<XCBuildSettingLayer> layers) {
			this.parent = parent;
			this.layers = layers;
		}

		@Override
		public String getName() {
			return parent.getName();
		}

		@Override
		public XCBuildSetting findNext() {
			if (layers.hasNext()) {
				return layers.next().find(this);
			} else {
				return parent.findNext();
			}
		}
	}

	@Override
	public Map<String, XCBuildSetting> findAll() {
		LinkedHashMap<String, XCBuildSetting> result = new LinkedHashMap<>();
		for (XCBuildSettingLayer layer : layers) {
			layer.findAll().forEach((k, v) -> {
				if (!result.containsKey(k)) {
					result.put(k, v);
				}
			});
		}
		return result;
	}
}
