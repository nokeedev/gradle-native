/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;

import java.util.Comparator;

final class PreferSharedBinaryLinkageComparator implements Comparator<BinaryLinkage> {
	@Override
	public int compare(BinaryLinkage lhs, BinaryLinkage rhs) {
		if (lhs.isShared() && rhs.isShared()) {
			return 0;
		} else if (lhs.isShared()) {
			return -1;
		} else if (rhs.isShared()) {
			return 1;
		}
		return 0;
	}
}
