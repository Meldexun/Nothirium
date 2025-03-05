package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;

public class OptifineTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.ChunkRenderManager", "allChanged", 0, method -> {
			MethodInsnNode createChunkRenderer = ASMUtil.first(method).methodInsn("createChunkRenderer").find();
			createChunkRenderer.owner = "meldexun/nothirium/mc/integration/Optifine";
		});
		registry.add("meldexun.nothirium.mc.renderer.chunk.SectionRenderCache", "calculateCombinedLight", ClassWriter.COMPUTE_FRAMES, method -> {
			method.instructions.insert(ASMUtil.last(method).opcode(Opcodes.IRETURN).findThenPrev().type(LabelNode.class).find(), ASMUtil.listWithLabel(label -> ASMUtil.listOf(
					new MethodInsnNode(Opcodes.INVOKESTATIC, "Config", "isDynamicLights", "()Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "state", "Lnet/minecraft/block/state/IBlockState;").index),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/block/state/IBlockState", "isOpaqueCube", "()Z", false),
					new JumpInsnNode(Opcodes.IFNE, label),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "pos", "Lnet/minecraft/util/math/BlockPos;").index),
					new VarInsnNode(Opcodes.ILOAD, ASMUtil.findLocalVariable(method, "light", "I").index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "net/optifine/DynamicLights", "getCombinedLight", "(Lnet/minecraft/util/math/BlockPos;I)I", false),
					new VarInsnNode(Opcodes.ISTORE, ASMUtil.findLocalVariable(method, "light", "I").index),
					label)));
		});
	}

}
