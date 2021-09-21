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
@Root(name = "ImportGroup")
public class VCXImportGroup {
	@Attribute
	@With String label;

	@Attribute(required = false)
	@With String condition;

	@ElementList(inline = true, required = false)
	List<VCXImport> imports;

	public static VCXImportGroup empty(String label) {
		return new VCXImportGroup(label, null, ImmutableList.of());
	}

	public static VCXImportGroup of(Iterable<VCXImport> imports) {
		return new VCXImportGroup(null, null, ImmutableList.copyOf(imports));
	}

	public static VCXImportGroup of(VCXImport... imports) {
		return new VCXImportGroup(null, null, ImmutableList.copyOf(imports));
	}
}
