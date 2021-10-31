package dev.nokee.language.base.testers;

import dev.nokee.language.base.HasDestinationDirectory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public interface HasDestinationDirectoryTester {
	HasDestinationDirectory subject();

	@Test
	default void hasDestinationDirectory() {
		assertThat("not null as per contract", subject().getDestinationDirectory(), notNullValue());
	}
}
