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
package dev.nokee.xcode.objects;

import com.google.common.base.Suppliers;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Reference to another object used by {@link PBXTargetDependency}.
 * Can reference a remote file by specifying the {@link PBXFileReference} to the remote project file, and the GID of the object within that file.
 */
public final class PBXContainerItemProxy extends PBXContainerItem {
	public enum ProxyType {
		TARGET_REFERENCE(1), // native target
		REFERENCE(2);

		private final int intValue;

		ProxyType(int intValue) {
			this.intValue = intValue;
		}

		public int getIntValue() {
			return intValue;
		}
	}

	private final Supplier<ContainerPortal> containerPortal;
	private final String remoteGlobalIDString;
	private final ProxyType proxyType;
	@Nullable private final String remoteInfo;

	private PBXContainerItemProxy(Supplier<ContainerPortal> containerPortal, String remoteGlobalIDString, ProxyType proxyType, @Nullable String remoteInfo) {
		this.containerPortal = containerPortal;
		this.remoteGlobalIDString = remoteGlobalIDString;
		this.proxyType = proxyType;
		this.remoteInfo = remoteInfo;
	}

	public ContainerPortal getContainerPortal() {
		return containerPortal.get();
	}

	public String getRemoteGlobalIDString() {
		return remoteGlobalIDString;
	}

	public ProxyType getProxyType() {
		return proxyType;
	}

	public Optional<String> getRemoteInfo() {
		return Optional.ofNullable(remoteInfo);
	}

	@Override
	public int stableHash() {
		return remoteGlobalIDString.hashCode();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private Supplier<ContainerPortal> containerPortal;
		private ProxyType proxyType;
		private String remoteGlobalId;
		private String remoteInfo;

		public Builder containerPortal(ContainerPortal containerPortal) {
			this.containerPortal = Suppliers.ofInstance(containerPortal);
			return this;
		}

		public Builder containerPortal(Supplier<ContainerPortal> containerPortal) {
			this.containerPortal = containerPortal;
			return this;
		}

		public Builder proxyType(ProxyType proxyType) {
			this.proxyType = proxyType;
			return this;
		}

		public Builder remoteGlobalId(String remoteGlobalId) {
			this.remoteGlobalId = remoteGlobalId;
			return this;
		}

		public Builder remoteInfo(String remoteInfo) {
			this.remoteInfo = remoteInfo;
			return this;
		}

		public PBXContainerItemProxy build() {
			return new PBXContainerItemProxy(requireNonNull(containerPortal, "'containerPortal' must not be null"), requireNonNull(remoteGlobalId, "'remoteGlobalId' must not be null"), requireNonNull(proxyType, "'proxyType' must not be null"), remoteInfo);
		}
	}

	/**
	 * Represent a container portal for a {@link PBXContainerItemProxy}.
	 */
	public interface ContainerPortal {}
}
