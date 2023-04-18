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
package dev.nokee.util.internal;

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.internal.artifacts.dsl.ArtifactFile;
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact;
import org.gradle.api.internal.artifacts.publish.DefaultPublishArtifact;
import org.gradle.api.internal.tasks.AbstractTaskDependency;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import java.io.File;
import java.util.Date;

public class LazyPublishArtifact implements PublishArtifact {
    private final Provider<?> provider;
    private final String version;
    private PublishArtifact delegate;

    public LazyPublishArtifact(Provider<?> provider) {
        this.provider = provider;
        this.version = null;
    }

    public LazyPublishArtifact(Provider<?> provider, String version) {
        this.provider = provider;
        this.version = version;
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public String getExtension() {
        return delegate().getExtension();
    }

    @Override
    public String getType() {
        return delegate().getType();
    }

    @Override
    public String getClassifier() {
        return delegate().getClassifier();
    }

    @Override
    public File getFile() {
        return delegate().getFile();
    }

    @Override
    public Date getDate() {
        return null;
    }

    private PublishArtifact delegate() {
        if (delegate == null) {
            Object value = provider.get();
            if (value instanceof FileSystemLocation) {
                FileSystemLocation location = (FileSystemLocation) value;
                delegate = fromFile(location.getAsFile());
            } else if (value instanceof File) {
                delegate = fromFile((File)value);
            } else if (value instanceof AbstractArchiveTask) {
                delegate = new ArchivePublishArtifact((AbstractArchiveTask)value);
            } else {
                throw new InvalidUserDataException(String.format("Cannot convert provided value (%s) to a file.", value));
            }
        }
        return delegate;
    }

    private PublishArtifact fromFile(File file) {
        ArtifactFile artifactFile = new ArtifactFile(file, version);
        return new DefaultPublishArtifact(artifactFile.getName(), artifactFile.getExtension(), artifactFile.getExtension(), artifactFile.getClassifier(), null, file);
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return new AbstractTaskDependency() {
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                context.add(provider);
            }
        };
    }
}
