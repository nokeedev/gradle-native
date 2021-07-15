package dev.nokee.model.core;

import java.util.function.Function;

@FunctionalInterface
public interface ModelProjectionBuilderAction<S> extends Function<ModelProjection.Builder, TypeAwareModelProjection.Builder<S>> {}
