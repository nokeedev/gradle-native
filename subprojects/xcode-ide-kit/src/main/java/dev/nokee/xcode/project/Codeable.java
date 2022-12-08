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
package dev.nokee.xcode.project;

import javax.annotation.Nullable;
import java.util.Map;

public interface Codeable {
	String isa();

	@Nullable
	String globalId();

	default int stableHash() {
		return 0;
	}

	<T> T tryDecode(CodingKey key);

	void encode(EncodeContext context);

	default boolean has(CodingKey key) {
		return tryDecode(key) != null;
	}

	interface EncodeContext {

		void base(Map<String, ?> fields);

		void gid(String globalID);

		void tryEncode(Map<CodingKey, ?> data);

		void noGid();
	}
}
