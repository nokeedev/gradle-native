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

import dev.nokee.xcode.objects.PBXProject;

import java.util.Optional;

public final class PBXObjectUnarchiver {
	private final CodingKeyCoders coders;

	public PBXObjectUnarchiver() {
		this(it -> Optional.ofNullable(KeyedCoders.DECODERS.get(it)));
	}

	public PBXObjectUnarchiver(CodingKeyCoders coders) {
		this.coders = coders;
	}

	public PBXProject decode(PBXProj proj) {
		return CodeablePBXProject.newInstance(new CachingKeyedObject(new PBXObjectReferenceKeyedObject(proj.getObjects(), proj.getObjects().getById(proj.getRootObject()), coders)));
	}
}
