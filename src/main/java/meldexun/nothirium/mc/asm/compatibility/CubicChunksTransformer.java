package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;

public class CubicChunksTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.util.Direction$1", "isFaceCulled", ClassWriter.COMPUTE_FRAMES, method -> {
			method.instructions.insert(ASMUtil.listOf(
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isCubicWorld", "()Z", false),
					new JumpInsnNode(Opcodes.IFNE, ASMUtil.first(method).type(JumpInsnNode.class).find().label)));
		});
		registry.add("meldexun.nothirium.util.Direction$2", "isFaceCulled", ClassWriter.COMPUTE_FRAMES, method -> {
			method.instructions.insert(ASMUtil.listOf(
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isCubicWorld", "()Z", false),
					new JumpInsnNode(Opcodes.IFNE, ASMUtil.first(method).type(JumpInsnNode.class).find().label)));
		});

		registry.add("meldexun.nothirium.mc.util.WorldUtil", "isSectionLoaded", "(Lnet/minecraft/world/World;III)Z", ClassWriter.COMPUTE_FRAMES, method -> {
			LocalVariableNode world = ASMUtil.findLocalVariable(method, "world", "Lnet/minecraft/world/World;");
			LocalVariableNode sectionX = ASMUtil.findLocalVariable(method, "sectionX", "I");
			LocalVariableNode sectionY = ASMUtil.findLocalVariable(method, "sectionY", "I");
			LocalVariableNode sectionZ = ASMUtil.findLocalVariable(method, "sectionZ", "I");

			method.instructions.insert(ASMUtil.listWithLabel(label -> ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, world.index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isCubicWorld", "(Lnet/minecraft/world/World;)Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new VarInsnNode(Opcodes.ALOAD, world.index),
					new VarInsnNode(Opcodes.ILOAD, sectionX.index),
					new VarInsnNode(Opcodes.ILOAD, sectionY.index),
					new VarInsnNode(Opcodes.ILOAD, sectionZ.index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isSectionLoaded", "(Lnet/minecraft/world/World;III)Z", false),
					new InsnNode(Opcodes.IRETURN),
					label)));
		});
		registry.add("meldexun.nothirium.mc.util.WorldUtil", "getSection", "(Lnet/minecraft/world/World;III)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;", ClassWriter.COMPUTE_FRAMES, method -> {
			LocalVariableNode world = ASMUtil.findLocalVariable(method, "world", "Lnet/minecraft/world/World;");
			LocalVariableNode sectionX = ASMUtil.findLocalVariable(method, "sectionX", "I");
			LocalVariableNode sectionY = ASMUtil.findLocalVariable(method, "sectionY", "I");
			LocalVariableNode sectionZ = ASMUtil.findLocalVariable(method, "sectionZ", "I");

			method.instructions.insert(ASMUtil.listWithLabel(label -> ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, world.index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isCubicWorld", "(Lnet/minecraft/world/World;)Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new VarInsnNode(Opcodes.ALOAD, world.index),
					new VarInsnNode(Opcodes.ILOAD, sectionX.index),
					new VarInsnNode(Opcodes.ILOAD, sectionY.index),
					new VarInsnNode(Opcodes.ILOAD, sectionZ.index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "getSection", "(Lnet/minecraft/world/World;III)Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;", false),
					new InsnNode(Opcodes.ARETURN),
					label)));
		});

		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunk", "markDirty", ClassWriter.COMPUTE_FRAMES, method -> {
			method.instructions.insert(ASMUtil.listOf(
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isCubicWorld", "()Z", false),
					new JumpInsnNode(Opcodes.IFNE, ASMUtil.first(method).opcode(Opcodes.RETURN).findThenNext().type(LabelNode.class).find())));
		});
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunk", "canCompile", ClassWriter.COMPUTE_FRAMES, method -> {
			method.instructions.insert(ASMUtil.listWithLabel(label -> ASMUtil.listOf(
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "isCubicWorld", "()Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/CubicChunks", "canCompile", "(Lmeldexun/nothirium/api/renderer/chunk/IRenderChunk;)Z", false),
					new InsnNode(Opcodes.IRETURN),
					label)));
		});
	}

}
