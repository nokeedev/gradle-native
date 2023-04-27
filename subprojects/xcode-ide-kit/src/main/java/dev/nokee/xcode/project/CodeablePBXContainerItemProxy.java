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

import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXContainerItemProxy extends AbstractCodeable implements PBXContainerItemProxy {
	public enum CodingKeys implements CodingKey {
		containerPortal,
		remoteGlobalIDString,
		proxyType,
		remoteInfo,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXContainerItemProxy(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public ContainerPortal getContainerPortal() {
		return tryDecode(CodingKeys.containerPortal);
	}

	@Override
	public String getRemoteGlobalIDString() {
		return tryDecode(CodingKeys.remoteGlobalIDString);
	}

	@Override
	public ProxyType getProxyType() {
		return tryDecode(CodingKeys.proxyType);
	}

	@Override
	public Optional<String> getRemoteInfo() {
		return getAsOptional(CodingKeys.remoteInfo);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public int stableHash() {
		return getRemoteGlobalIDString().hashCode();
	}

	public static CodeablePBXContainerItemProxy newInstance(KeyedObject delegate) {
		return new CodeablePBXContainerItemProxy(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
