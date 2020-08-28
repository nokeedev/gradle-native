package dev.nokee.buildadapter.cmake.internal.fileapi;

import lombok.Value;

import java.util.List;

@Value
public class Index {
	List<ObjectKindReference> objects;

	@Value
	public static class ObjectKindReference {
		String jsonFile;
		ApiVersion version;

		@Value
		public static class ApiVersion {
			Integer major;
			Integer minor;
		}
	}
}
