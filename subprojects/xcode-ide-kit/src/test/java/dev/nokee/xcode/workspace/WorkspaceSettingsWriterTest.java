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
package dev.nokee.xcode.workspace;

import dev.nokee.xcode.PropertyListVersion;
import dev.nokee.xcode.PropertyListWriter;
import dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.BooleanOption;
import dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.StringOption;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceSettingsWriterTest {
	@Mock PropertyListWriter writer;
	@InjectMocks WorkspaceSettingsWriter subject;

	@Test
	void canWriteWorkspaceSettingsWithoutOptions() {
		subject.write(WorkspaceSettings.builder().build());
		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(PropertyListVersion.VERSION_00);
		inOrder.verify(writer).writeEmptyDictionary();
		inOrder.verify(writer).writeEndDocument();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	void canWriteWorkspaceSettingsWithBooleanOption() {
		subject.write(WorkspaceSettings.builder().put(BooleanOption.TRUE).build());
		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(PropertyListVersion.VERSION_00);
		inOrder.verify(writer).writeStartDictionary(1);
		inOrder.verify(writer).writeDictionaryKey("BooleanOption");
		inOrder.verify(writer).writeBoolean(true);
		inOrder.verify(writer).writeEndDictionary();
		inOrder.verify(writer).writeEndDocument();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	void canWriteWorkspaceSettingsWithStringOption() {
		subject.write(WorkspaceSettings.builder().put(StringOption.Value).build());
		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(PropertyListVersion.VERSION_00);
		inOrder.verify(writer).writeStartDictionary(1);
		inOrder.verify(writer).writeDictionaryKey("StringOption");
		inOrder.verify(writer).writeString("Value");
		inOrder.verify(writer).writeEndDictionary();
		inOrder.verify(writer).writeEndDocument();
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	void canWriteWorkspaceSettingsWithMultipleOptions() {
		subject.write(WorkspaceSettings.builder().put(StringOption.OtherValue).put(BooleanOption.FALSE).build());
		val inOrder = Mockito.inOrder(writer);
		inOrder.verify(writer).writeStartDocument(PropertyListVersion.VERSION_00);
		inOrder.verify(writer).writeStartDictionary(2);
		inOrder.verify(writer).writeDictionaryKey("StringOption");
		inOrder.verify(writer).writeString("OtherValue");
		inOrder.verify(writer).writeDictionaryKey("BooleanOption");
		inOrder.verify(writer).writeBoolean(false);
		inOrder.verify(writer).writeEndDictionary();
		inOrder.verify(writer).writeEndDocument();
		inOrder.verifyNoMoreInteractions();
	}
}
