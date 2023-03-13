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
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

@EqualsAndHashCode
public final class CodeablePBXContainerItemProxy implements PBXContainerItemProxy, Codeable {
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

	private final KeyedObject delegate;

	public CodeablePBXContainerItemProxy(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public ContainerPortal getContainerPortal() {
		return delegate.tryDecode(CodingKeys.containerPortal);
	}

	@Override
	public String getRemoteGlobalIDString() {
		return delegate.tryDecode(CodingKeys.remoteGlobalIDString);
	}

	@Override
	public ProxyType getProxyType() {
		return delegate.tryDecode(CodingKeys.proxyType);
	}

	@Override
	public Optional<String> getRemoteInfo() {
		return orEmpty(delegate.tryDecode(CodingKeys.remoteInfo));
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public int stableHash() {
		return getRemoteGlobalIDString().hashCode();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeablePBXContainerItemProxy newInstance(KeyedObject delegate) {
		return new CodeablePBXContainerItemProxy(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
