package dev.nokee.fixtures;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollectionTestFixture {
	public static <T> T one(Iterable<T> c) {
		Iterator<T> iterator = c.iterator();
		assertTrue("collection needs to have one element, was empty", iterator.hasNext());
		T result = iterator.next();
		assertFalse("collection needs to only have one element, more than one element found", iterator.hasNext());
		return result;
	}
}
