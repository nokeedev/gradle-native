package dev.nokee.platform.nativebase.internal.repositories;

import javax.annotation.Nullable;

public interface FrameworkResolver {
	@Nullable
	byte[] resolve(String path);
}
