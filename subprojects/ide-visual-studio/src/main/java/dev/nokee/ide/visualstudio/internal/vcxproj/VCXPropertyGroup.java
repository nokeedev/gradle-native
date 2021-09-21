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
import org.simpleframework.xml.Root;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Root(name = "PropertyGroup")
public class VCXPropertyGroup {
	@Attribute(required = false)
	@With String condition;

	@Attribute(required = false)
	@With String label;

	@ElementList(inline = true)
	List<VCXProperty> properties;

	public static VCXPropertyGroup empty(String label) {
		return new VCXPropertyGroup(null, label, ImmutableList.of());
	}

	public static VCXPropertyGroup of(Iterable<VCXProperty> properties) {
		return new VCXPropertyGroup(null, null, ImmutableList.copyOf(properties));
	}

	public static VCXPropertyGroup of(VCXProperty... properties) {
		return new VCXPropertyGroup(null, null, ImmutableList.copyOf(properties));
	}
}
