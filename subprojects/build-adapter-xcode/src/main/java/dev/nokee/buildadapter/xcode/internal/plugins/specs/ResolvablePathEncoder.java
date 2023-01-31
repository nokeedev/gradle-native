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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.project.ValueEncoder;
import dev.nokee.xcode.project.coders.CoderType;
import lombok.EqualsAndHashCode;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.nio.file.Path;

@EqualsAndHashCode
public final class ResolvablePathEncoder implements ValueEncoder<ResolvablePathEncoder.LocationSpec, Object> {
	private final ValueEncoder<PBXReference, Object> delegate;

	public ResolvablePathEncoder(ValueEncoder<PBXReference, Object> delegate) {
		this.delegate = delegate;
	}

	@Override
	public ResolvablePathEncoder.LocationSpec encode(Object value, Context context) {
		return new LocationPBXReferenceSpec(delegate.encode(value, context));
	}

	@Override
	public CoderType<?> getEncodeType() {
		return CoderType.of(Path.class);
	}

	public interface LocationSpec {
		FileCollection get();

		LocationSpec resolve(ResolveContext context);

		interface ResolveContext {
			FileCollection resolve(PBXReference reference);
		}
	}

	@EqualsAndHashCode
	public static final class LocationPBXReferenceSpec implements LocationSpec {
		private final PBXReference fileReference;

		private LocationPBXReferenceSpec(PBXReference fileReference) {
			this.fileReference = fileReference;
		}

		@Override
		public LocationSpec resolve(ResolveContext context) {
			return new LocationResolvedFileTreeSpec(context.resolve(fileReference));
		}

		@Override
		public FileCollection get() {
			throw new UnsupportedOperationException("Must realize first");
		}
	}

	@EqualsAndHashCode
	public static final class LocationResolvedFileTreeSpec implements LocationSpec {
		private final FileCollection files;

		private LocationResolvedFileTreeSpec(FileCollection files) {
			this.files = files;
		}

		@Override
		public FileCollection get() {
			return files;
		}

		@Override
		public LocationSpec resolve(ResolveContext context) {
			return this;
		}
	}
}
