/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.visualstudio.internal.vcxproj;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public abstract class VCXClInclude {
	private VCXClInclude() {}

	@Value
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	@Root(name = "ClInclude")
	public static class Item implements VCXItem {
		@Attribute
		String include;

		@Element(required = false)
		@With String filter;

		public static Item of(String include) {
			return new Item(include, null);
		}
	}
}
