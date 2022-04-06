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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.files.PBXReference;

import java.util.Map;

/**
 * File referenced by a build phase, unique to each build phase.
 *
 * Contains a dictionary {@link #settings} which holds additional information to be interpreted by
 * the particular phase referencing this object, e.g.:
 *
 * - {@link PBXSourcesBuildPhase } may read <code>{"COMPILER_FLAGS": "-foo"}</code> and interpret
 * that this file should be compiled with the additional flag {@code "-foo" }.
 */
public final class PBXBuildFile extends PBXProjectItem {
    private final PBXReference fileRef;
    private final ImmutableMap<String, ?> settings;

	public PBXBuildFile(PBXReference fileRef) {
		this(fileRef, ImmutableMap.of());
	}

    public PBXBuildFile(PBXReference fileRef, ImmutableMap<String, ?> settings) {
        this.fileRef = Preconditions.checkNotNull(fileRef);
        this.settings = settings;
    }

    public PBXReference getFileRef() {
        return fileRef;
    }

    public Map<String, ?> getSettings() {
        return settings;
    }

    @Override
    public int stableHash() {
        return fileRef.stableHash();
    }

    @Override
    public String toString() {
        return String.format("%s fileRef=%s settings=%s", super.toString(), fileRef, settings);
    }
}
