package dev.nokee.model.streams;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represent the source of a stream in term of available elements.
 *
 * @param <E_IN>  type of elements to source
 */
interface Source<E_IN> extends Supplier<Stream<E_IN>> {}
