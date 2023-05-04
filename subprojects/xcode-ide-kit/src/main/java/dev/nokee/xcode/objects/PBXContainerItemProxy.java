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

import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.project.CodeablePBXContainerItemProxy;
import dev.nokee.xcode.project.CodeablePBXContainerItemProxy.CodingKeys;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.KeyedCoders;

import java.util.Optional;

import static dev.nokee.xcode.project.DefaultKeyedObject.key;

/**
 * Reference to another object used by {@link PBXTargetDependency}.
 * Can reference a remote file by specifying the {@link PBXFileReference} to the remote project file, and the GID of the object within that file.
 */
public interface PBXContainerItemProxy extends PBXContainerItem {
	enum ProxyType {
		TARGET_REFERENCE(1), // native target
		FILE_REFERENCE(2);

		private final int intValue;

		ProxyType(int intValue) {
			this.intValue = intValue;
		}

		public int getIntValue() {
			return intValue;
		}

		public static ProxyType valueOf(int value) {
			for (ProxyType candidate : values()) {
				if (candidate.intValue == value) {
					return candidate;
				}
			}
			throw new IllegalArgumentException(String.format("value '%d' is not known", value));
		}
	}

	ContainerPortal getContainerPortal();

	String getRemoteGlobalIDString();

	ProxyType getProxyType();

	Optional<String> getRemoteInfo();

	static Builder builder() {
		return new Builder();
	}

	final class Builder {
		private final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();

		public Builder() {
			builder.put(KeyedCoders.ISA, "PBXContainerItemProxy");
			builder.requires(key(CodingKeys.containerPortal));
			builder.requires(key(CodingKeys.remoteGlobalIDString));
			builder.requires(key(CodingKeys.proxyType));
		}

		public Builder containerPortal(ContainerPortal containerPortal) {
			builder.put(CodingKeys.containerPortal, containerPortal);
			return this;
		}

		public Builder proxyType(ProxyType proxyType) {
			builder.put(CodingKeys.proxyType, proxyType);
			return this;
		}

		public Builder remoteGlobalId(String remoteGlobalId) {
			builder.put(CodingKeys.remoteGlobalIDString, remoteGlobalId);
			return this;
		}

		public Builder remoteInfo(String remoteInfo) {
			builder.put(CodingKeys.remoteInfo, remoteInfo);
			return this;
		}

		public PBXContainerItemProxy build() {
			return new CodeablePBXContainerItemProxy(builder.build());
		}
	}

	/**
	 * Represent a container portal for a {@link PBXContainerItemProxy}.
	 */
	interface ContainerPortal {
		<R> R accept(Visitor<R> visitor);

		interface Visitor<R> {
			R visit(PBXProject container);
			R visit(PBXFileReference container);
		}
	}
}
