package dev.nokee.platform.nativebase.tasks.internal;

import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public class LinkExecutableTask extends org.gradle.nativeplatform.tasks.LinkExecutable implements LinkExecutable {
}
