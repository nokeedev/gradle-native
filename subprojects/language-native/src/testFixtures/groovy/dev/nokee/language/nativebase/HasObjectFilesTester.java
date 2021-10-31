package dev.nokee.language.nativebase;

import org.gradle.api.file.ConfigurableFileCollection;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public interface HasObjectFilesTester {
	HasObjectFiles subject();

	@Test
	default void hasObjectFiles() {
		assertThat("not null as per contract", subject().getObjectFiles(), notNullValue(ConfigurableFileCollection.class));
	}
}
