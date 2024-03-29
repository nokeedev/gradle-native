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

import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.CodeablePBXAggregateTarget;
import dev.nokee.xcode.project.CodeablePBXLegacyTarget;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class TargetDecoder<T extends PBXTarget & Codeable> implements ValueDecoder<T, KeyedObject> {
	@Override
	public T decode(KeyedObject object, Context context) {
		return Select.newInstance(KeyedObject::isa)
			.forCase("PBXAggregateTarget", CodeablePBXAggregateTarget::newInstance)
			.forCase("PBXLegacyTarget", CodeablePBXLegacyTarget::newInstance)
			.forCase("PBXNativeTarget", CodeablePBXNativeTarget::newInstance)
			.select(object);
	}
}
