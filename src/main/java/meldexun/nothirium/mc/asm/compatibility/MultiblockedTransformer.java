package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;

public class MultiblockedTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile", "renderBlockState", ClassWriter.COMPUTE_FRAMES, method -> {
			LabelNode skip = ASMUtil.first(method).methodInsnObf("renderBlock", "func_175018_a").findThenNext().type(LabelNode.class).find();
			method.instructions.insert(ASMUtil.first(method).methodInsnObf("getWorldRendererByLayer", "func_179038_a").findThenPrev().type(LabelNode.class).find(), ASMUtil.listOf(
					new FieldInsnNode(Opcodes.GETSTATIC, "com/cleanroommc/multiblocked/persistence/MultiblockWorldSavedData", "isBuildingChunk", "Ljava/lang/ThreadLocal;"),
					new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;"),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/ThreadLocal", "set", "(Ljava/lang/Object;)V", false),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "pos", "Lnet/minecraft/util/math/BlockPos;").index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "com/cleanroommc/multiblocked/persistence/MultiblockWorldSavedData", "isModelDisabled", "(Lnet/minecraft/util/math/BlockPos;)Z", false),
					new JumpInsnNode(Opcodes.IFNE, skip)));
			method.instructions.insert(skip, ASMUtil.listOf(
					new FieldInsnNode(Opcodes.GETSTATIC, "com/cleanroommc/multiblocked/persistence/MultiblockWorldSavedData", "isBuildingChunk", "Ljava/lang/ThreadLocal;"),
					new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;"),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/ThreadLocal", "set", "(Ljava/lang/Object;)V", false)));
		});
	}

}
