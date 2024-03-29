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

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;

import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXTargetDependency extends AbstractCodeable implements PBXTargetDependency {
	public enum CodingKeys implements CodingKey {
		name,
		target,
		targetProxy,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXTargetDependency(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public Optional<String> getName() {
		return getAsOptional(CodingKeys.name);
	}

	@Override
	public Optional<PBXTarget> getTarget() {
		return getAsOptional(CodingKeys.target);
	}

	@Override
	public PBXContainerItemProxy getTargetProxy() {
		return getOrNull(CodingKeys.targetProxy);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public int stableHash() {
		return ((Codeable) getTargetProxy()).stableHash();
	}

	public static CodeablePBXTargetDependency newInstance(KeyedObject delegate) {
		return new CodeablePBXTargetDependency(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
