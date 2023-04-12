/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import lombok.val;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public final class DefaultWritable<T> implements Writable<T> {
	private final ObjectWriter.Factory<T> factory;
	private final T delegate;

	public DefaultWritable(ObjectWriter.Factory<T> factory, T delegate) {
		this.factory = factory;
		this.delegate = delegate;
	}

	@Override
	public Path writeTo(Path path) throws IOException {
		val writer = factory.create(path);
		try {
			writer.write(delegate);
		} finally {
			if (writer instanceof Closeable) {
				((Closeable) writer).close();
			}
		}
		return path;
	}

	@Override
	public void writeTo(Writer out) throws IOException {
		val writer = factory.create(out);
		try {
			writer.write(delegate);
		} finally {
			if (writer instanceof Closeable) {
				((Closeable) writer).close();
			}
		}
	}
}
