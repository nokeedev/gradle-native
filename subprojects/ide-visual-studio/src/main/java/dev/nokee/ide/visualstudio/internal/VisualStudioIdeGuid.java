package dev.nokee.ide.visualstudio.internal;

import lombok.NonNull;
import lombok.Value;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Value
public class VisualStudioIdeGuid {
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
		return "{" + uuid.toString() + "}";
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

	public static VisualStudioIdeGuid stableGuidFrom(File file) {
		return new VisualStudioIdeGuid(UUID.nameUUIDFromBytes(file.getAbsolutePath().getBytes()));
	}

	public static VisualStudioIdeGuid stableGuidFrom(Provider<? extends FileSystemLocation> provider) {
		return stableGuidFrom(provider.get().getAsFile());
	}
}
