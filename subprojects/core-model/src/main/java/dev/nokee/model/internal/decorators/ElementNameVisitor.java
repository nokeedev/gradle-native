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

package dev.nokee.model.internal.decorators;

import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.MainName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.utils.Optionals;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;

public final class ElementNameVisitor {
	private final MethodVisitor mv;

	public ElementNameVisitor(MethodVisitor mv) {
		this.mv = mv;
	}

	public void visitElementName(@Nullable ElementName elementName) {
		if (elementName == null) {
			mv.visitInsn(Opcodes.ACONST_NULL);
		} else if (elementName instanceof TaskName) {
			final TaskName taskName = (TaskName) elementName;
			mv.visitLdcInsn(taskName.getVerb());
			Optionals.ifPresentOrElse(taskName.getObject(), object -> {
				mv.visitLdcInsn(object);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(TaskName.class), "of", "(Ljava/lang/String;Ljava/lang/String;)Ldev/nokee/model/internal/names/TaskName;", false);
			}, () -> {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(TaskName.class), "of", "(Ljava/lang/String;)Ldev/nokee/model/internal/names/TaskName;", false);
			});
		} else if (elementName instanceof MainName) {
			mv.visitLdcInsn(elementName.toString());
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(ElementName.class), "ofMain", "(Ljava/lang/String;)Ldev/nokee/model/internal/names/ElementName;", true);
		} else {
			mv.visitLdcInsn(elementName.toString());
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(ElementName.class), "of", "(Ljava/lang/String;)Ldev/nokee/model/internal/names/ElementName;", true);
		}
	}
}
