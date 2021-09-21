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

import lombok.Value;
import org.simpleframework.xml.*;

import java.util.List;

@Value
@Root(name = "Project")
@Namespace(reference = "http://schemas.microsoft.com/developer/msbuild/2003")
public class VCXProject {
	@Attribute(required = false)
	String defaultTargets;

	@Attribute(required = false)
	String toolVersions;

	@ElementListUnion({
		@ElementList(inline = true, type = VCXItemGroup.class),
		@ElementList(inline = true, type = VCXPropertyGroup.class),
		@ElementList(inline = true, type = VCXImport.class),
		@ElementList(inline = true, type = VCXImportGroup.class),
		@ElementList(inline = true, type = VCXItemDefinitionGroup.class),
		@ElementList(inline = true, type = VCXTarget.class)
	})
	List<Object> nodes;
}
