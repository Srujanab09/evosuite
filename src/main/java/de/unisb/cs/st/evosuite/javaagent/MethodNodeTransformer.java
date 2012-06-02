/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Gordon Fraser
 * 
 */
public class MethodNodeTransformer {

	public void transform(MethodNode mn) {

		Set<AbstractInsnNode> originalNodes = new HashSet<AbstractInsnNode>();
		AbstractInsnNode node = mn.instructions.getFirst();
		while (node != mn.instructions.getLast()) {
			originalNodes.add(node);
			node = node.getNext();
		}

		//int currentIndex = 0;

		node = mn.instructions.getFirst();
		//while (currentIndex < mn.instructions.size()) {
		boolean finished = false;
		while (!finished) {
			//while (node != mn.instructions.getLast()) {
			//node = mn.instructions.get(currentIndex);
			//int oldLength = mn.instructions.size();
			//			BytecodeInstruction insn = BytecodeInstructionPool.getInstruction(className,
			//			                                                                  mn.name
			//			                                                                          + mn.desc,
			//			                                                                  node);
			//			if (insn == null) {
			//				//			if (!originalNodes.contains(node)) {
			//				System.out.println("Node not present in original stuff " + node);
			//				// Only transform nodes present in original method
			//			} else 
			if (node instanceof MethodInsnNode) {
				node = transformMethodInsnNode(mn, (MethodInsnNode) node);
			} else if (node instanceof VarInsnNode) {
				node = transformVarInsnNode(mn, (VarInsnNode) node);
			} else if (node instanceof FieldInsnNode) {
				node = transformFieldInsnNode(mn, (FieldInsnNode) node);
			} else if (node instanceof InsnNode) {
				node = transformInsnNode(mn, (InsnNode) node);
			} else if (node instanceof TypeInsnNode) {
				node = transformTypeInsnNode(mn, (TypeInsnNode) node);
			} else if (node instanceof JumpInsnNode) {
				node = transformJumpInsnNode(mn, (JumpInsnNode) node);
			} else if (node instanceof LabelNode) {
				node = transformLabelNode(mn, (LabelNode) node);
			} else if (node instanceof IntInsnNode) {
				node = transformIntInsnNode(mn, (IntInsnNode) node);
			} else if (node instanceof MultiANewArrayInsnNode) {
				node = transformMultiANewArrayInsnNode(mn, (MultiANewArrayInsnNode) node);
			}
			//currentIndex += mn.instructions.size() - oldLength;
			//currentIndex++;

			if (node == mn.instructions.getLast()) {
				finished = true;
			} else {
				node = node.getNext();
			}
		}
	}

	protected AbstractInsnNode transformMethodInsnNode(MethodNode mn,
	        MethodInsnNode methodNode) {
		return methodNode;
	}

	protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
		return varNode;
	}

	protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
	        FieldInsnNode fieldNode) {
		return fieldNode;
	}

	protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
		return insnNode;
	}

	protected AbstractInsnNode transformTypeInsnNode(MethodNode mn, TypeInsnNode typeNode) {
		return typeNode;
	}

	protected AbstractInsnNode transformJumpInsnNode(MethodNode mn, JumpInsnNode jumpNode) {
		return jumpNode;
	}

	protected AbstractInsnNode transformLabelNode(MethodNode mn, LabelNode labelNode) {
		return labelNode;
	}

	protected AbstractInsnNode transformIntInsnNode(MethodNode mn, IntInsnNode intInsnNode) {
		return intInsnNode;
	}

	protected AbstractInsnNode transformMultiANewArrayInsnNode(MethodNode mn,
	        MultiANewArrayInsnNode arrayInsnNode) {
		return arrayInsnNode;
	}
}
