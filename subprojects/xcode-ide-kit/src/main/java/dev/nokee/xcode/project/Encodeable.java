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

public interface Encodeable {
	String isa();

	@Nullable
	String globalId();

	long age();

	default int stableHash() {
		return 0;
	}

	void encode(EncodeContext context);

	interface EncodeContext {

		void base(Map<String, ?> fields);

		void gid(String globalID);

		void tryEncode(Map<CodingKey, ?> data);

		void noGid();
	}
}
