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
package dev.nokee.buildadapter.cocoapods.internal;

import lombok.EqualsAndHashCode;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@EqualsAndHashCode
final class Podfile implements Serializable {
	private /*final*/ File location;
	private Content content;

	public Podfile(File location) {
		this.location = location;
	}

	public boolean exists() {
		return location.exists();
	}

	public Path getLocation() {
		return location.toPath();
	}

	public Content getContent() {
		if (content == null) {
			content = Content.load(location.toPath());
		}
		return content;
	}

	@EqualsAndHashCode
	public static final class Content implements Serializable {
		private final byte[] content;

		public Content(byte[] content) {
			this.content = content;
		}

		private static Content load(Path location) {
			try {
				if (Files.exists(location)) {
					return new Content(Files.readAllBytes(location));
				} else {
					return new Content(new byte[0]);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private void readObject(ObjectInputStream inStream) throws ClassNotFoundException, IOException {
		location = (File) inStream.readObject();
		content = (Content) inStream.readObject();
	}

	private void writeObject(ObjectOutputStream outStream) throws IOException {
		outStream.writeObject(location);
		outStream.writeObject(content);
	}

	public static Podfile of(File location) {
		return new Podfile(location);
	}
}
