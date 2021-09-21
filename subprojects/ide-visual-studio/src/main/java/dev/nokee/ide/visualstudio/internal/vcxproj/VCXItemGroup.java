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

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Root(name = "ItemGroup")
public class VCXItemGroup {
	@Attribute(required = false)
	@With String label;

	@ElementListUnion({
		@ElementList(inline = true, type = VCXProjectConfiguration.class),
		@ElementList(inline = true, type = VCXClCompile.Item.class),
		@ElementList(inline = true, type = VCXClInclude.Item.class),
		@ElementList(inline = true, type = VCXNone.class),
		@ElementList(inline = true, type = VCXFilter.class)
	})
	List<? extends VCXItem> items;

	public static VCXItemGroup empty() {
		return new VCXItemGroup(null, ImmutableList.of());
	}

	public static VCXItemGroup of(Iterable<? extends VCXItem> items) {
		return new VCXItemGroup(null, ImmutableList.copyOf(items));
	}

	public static VCXItemGroup of(VCXItem... items) {
		return new VCXItemGroup(null, ImmutableList.copyOf(items));
	}
}
