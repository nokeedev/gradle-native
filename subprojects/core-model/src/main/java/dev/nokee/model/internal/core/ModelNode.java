/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model.internal.core;

/**
 * A node in the model.
 */
// TODO: There is 3 concept mixed in here that we will need to extract:
//  1) The model node itself, which contains path, state, projections and some convenience methods for walking the adjacent model nodes
//  2) The discovery mechanic which allows users to register other nodes to this node
//  3) The configuration mechanic which allows users to apply ModelAction to the node and descendants
//  Each of these concept should only have access to a subset of the functionality here.
//  For example, during discovery, it should not be possible to change the state of the node as well as "getting" a projection.
//  You should be able to call register(NodeRegistration) and query "what are the projection types" (but not the projection instance).
//  During configuration, it should not be allowed to call register(NodeRegistration).
//  We can disable those methods but that would be a hack.
//  Instead we should extract the concept into the proper interface and classes.
//  For now, we will move forward with the current implementation and see what will become obvious as we use the APIs.
//  NOTE: It's also important to note that when accessing ModelNode from ModelLookup, we have access to all three mechanic mixed-in.
//    We should not allow users arbitrarily accessing every mechanic of the node.
//    It should be a truly immutable node they access...
//    Maybe at the worst they can attach a configuration on the node but that can be problematic if we "finalize" the node,
//      aka prevent further changes to the node.
//    Actually, we shouldn't allow attaching configuration (applyTo, applyToSelf).
//    Instead users should go through the ModelRegistry for that and access a thin layer that gives access to the allowed query and apply methods
public final class ModelNode implements Entity {
	private final ModelEntityId id = ModelEntityId.nextId();

	public ModelEntityId getId() {
		return id;
	}

	@Override
	public String toString() {
		return "entity '" + id + "'";
	}
}
