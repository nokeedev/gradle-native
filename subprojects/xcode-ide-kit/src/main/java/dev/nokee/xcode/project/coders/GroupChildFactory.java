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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.CodeableObjectFactory;
import dev.nokee.xcode.project.CodeablePBXFileReference;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.CodeablePBXReferenceProxy;
import dev.nokee.xcode.project.CodeablePBXVariantGroup;
import dev.nokee.xcode.project.CodeableXCVersionGroup;
import dev.nokee.xcode.project.KeyedObject;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class GroupChildFactory<T extends GroupChild & Codeable> implements CodeableObjectFactory<T> {
	@Override
	@SuppressWarnings("unchecked")
	public T create(KeyedObject map) {
		switch (map.isa()) {
			case "PBXFileReference": return (T) CodeablePBXFileReference.newInstance(map);
			case "PBXGroup": return (T) CodeablePBXGroup.newInstance(map);
			case "PBXReferenceProxy": return (T) CodeablePBXReferenceProxy.newInstance(map);
			case "PBXVariantGroup": return (T) CodeablePBXVariantGroup.newInstance(map);
			case "XCVersionGroup": return (T) CodeableXCVersionGroup.newInstance(map);
			default: throw new UnsupportedOperationException();
		}
	}
}
