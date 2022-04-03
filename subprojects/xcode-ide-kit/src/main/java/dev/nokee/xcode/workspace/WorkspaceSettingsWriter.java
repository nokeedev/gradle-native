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
package dev.nokee.xcode.workspace;

import com.google.common.collect.Iterables;
import dev.nokee.xcode.PropertyListVersion;
import dev.nokee.xcode.PropertyListWriter;

import java.io.Closeable;
import java.io.IOException;

public final class WorkspaceSettingsWriter implements Closeable {
	private final PropertyListWriter delegate;

	public WorkspaceSettingsWriter(PropertyListWriter delegate) {
		this.delegate = delegate;
	}

	public void write(WorkspaceSettings o) {
		delegate.writeStartDocument(PropertyListVersion.VERSION_00);
		if (Iterables.isEmpty(o)) {
			delegate.writeEmptyDictionary();
		} else {
			delegate.writeStartDictionary(Iterables.size(o));
			for (WorkspaceSettings.Option<?> option : o) {
				delegate.writeDictionaryKey(option.getName());
				final Object value = option.get();
				if (value instanceof Boolean) {
					delegate.writeBoolean((boolean) value);
				} else if (value instanceof CharSequence) {
					delegate.writeString((CharSequence) value);
				} else {
					throw new UnsupportedOperationException();
				}
			}
			delegate.writeEndDictionary();
		}
		delegate.writeEndDocument();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
