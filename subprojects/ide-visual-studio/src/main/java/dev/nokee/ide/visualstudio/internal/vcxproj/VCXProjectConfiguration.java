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

import dev.nokee.ide.visualstudio.VisualStudioIdeProjectConfiguration;
import lombok.Value;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Value(staticConstructor = "of")
@Root(name = "ProjectConfiguration")
public class VCXProjectConfiguration implements VCXItem {
	@Attribute
	public String getInclude() {
		return configuration + "|" + platform;
	}

	@Element
	String configuration;

	@Element
	String platform;

	public static VCXProjectConfiguration of(VisualStudioIdeProjectConfiguration projectConfiguration) {
		return new VCXProjectConfiguration(projectConfiguration.getConfiguration().getIdentifier(), projectConfiguration.getPlatform().getIdentifier());
	}
}
