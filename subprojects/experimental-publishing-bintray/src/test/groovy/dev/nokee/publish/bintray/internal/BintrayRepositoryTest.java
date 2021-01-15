package dev.nokee.publish.bintray.internal;

import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.net.URI;
import java.net.URISyntaxException;

import static dev.nokee.publish.bintray.internal.BintrayCredentials.of;
import static dev.nokee.publish.bintray.internal.BintrayPackageName.of;
import static dev.nokee.publish.bintray.internal.BintrayTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Subject(BintrayRepository.class)
class BintrayRepositoryTest {
	@Test
	void canCreateBintrayRepositoryFromRepositoryDeclaration() throws URISyntaxException {
		val repository = new BintrayRepository(bintrayUrl("foo/examples"), of("eve"), of("bob", "alice"));
		assertThat(repository.getUrl(), equalTo(new URI("https://dl.bintray.com/foo/examples")));
		assertThat(repository.getPackageName(), equalTo(of("eve")));
		assertThat(repository.getCredentials().getBintrayUser(), equalTo("bob"));
		assertThat(repository.getCredentials().getBintrayKey(), equalTo("alice"));
	}
}
