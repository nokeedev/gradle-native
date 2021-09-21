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
package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.visualstudio.VisualStudioIdeGuid;
import lombok.NonNull;
import lombok.Value;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Value
public class DefaultVisualStudioIdeGuid implements VisualStudioIdeGuid {
	@NonNull UUID uuid;

	/**
	 * Return the GUID as a Java UUID object.
	 *
	 * @return an {@link UUID} instance, never null.
	 */
	public UUID getAsJavaUuid() {
		return uuid;
	}

	/**
	 * Returns the GUID as Visual Studio IDE wants it, that is wrapped in mustache braces.
	 *
	 * @return the GUID wrapped in curly braces, never null.
	 */
	public String getAsString() {
		return "{" + uuid.toString().toUpperCase() + "}";
	}

	/**
	 * Returns a base64 shorten version of the GUID.
	 *
	 * @return the GUID base64 encoded, never null.
	 */
	public String getAsBase64() {
		return Base64.getUrlEncoder().encodeToString(getAsBytes());
	}

	/**
	 * Returns the GUID as a byte array.
	 *
	 * @return a byte array of the GUID, never null.
	 */
	public byte[] getAsBytes() {
		ByteBuffer result = ByteBuffer.allocate(16);
		result.putLong(uuid.getMostSignificantBits());
		result.putLong(uuid.getLeastSignificantBits());
		return result.array();
	}

	@Override
	public String toString() {
		return getAsString();
	}

	public static DefaultVisualStudioIdeGuid stableGuidFrom(File file) {
		return new DefaultVisualStudioIdeGuid(UUID.nameUUIDFromBytes(file.getAbsolutePath().getBytes()));
	}

	public static DefaultVisualStudioIdeGuid stableGuidFrom(Provider<? extends FileSystemLocation> provider) {
		return stableGuidFrom(provider.get().getAsFile());
	}
}
