package dev.nokee.platform.nativebase.internal.repositories;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GradleModuleMetadata {
	@NonNull String formatVersion;
	@NonNull List<Variant> variants;

	public static GradleModuleMetadata of() {
		return of(ImmutableList.of());
	}

	public static GradleModuleMetadata of(@NonNull List<Variant> variants) {
		return new GradleModuleMetadata("1.0", variants);
	}

	@Value
	public static class Variant {
		@NonNull String name;
		@NonNull Map<String, Object> attributes;
		@NonNull List<File> files;
		@NonNull List<Capability> capabilities;

		@Value
		@AllArgsConstructor(access = AccessLevel.PRIVATE)
		public static class File {
			@NonNull String name;
			@NonNull String url;
			@NonNull String size;
			@NonNull String sha1;
			@NonNull String md5;

			public static File ofLocalFile(java.io.File f) {
				return new File(f.getName() + ".localpath",
					f.getName() + ".localpath",
					String.valueOf(f.getAbsolutePath().getBytes().length),
					Hashing.sha1().hashString(f.getAbsolutePath(), Charset.defaultCharset()).toString(),
					Hashing.md5().hashString(f.getAbsolutePath(), Charset.defaultCharset()).toString());
			}
		}

		@Value
		public static class Capability {
			String group;
			String name;
			String version;
		}
	}
}
