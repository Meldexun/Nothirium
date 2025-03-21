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
import meldexun.nothirium.mc.asm.NothiriumClassTransformer;

public class OptifineTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.ChunkRenderManager", "allChanged", 0, method -> {
			MethodInsnNode createChunkRenderer = ASMUtil.first(method).methodInsn("createChunkRenderer").find();
			createChunkRenderer.owner = "meldexun/nothirium/mc/integration/Optifine";
		});
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile", "renderBlockState", ClassWriter.COMPUTE_FRAMES, method -> {
			LabelNode start = new LabelNode();
			LabelNode end = new LabelNode();
			ASMUtil.addLocalVariable(method, "renderEnv", "Lnet/optifine/render/RenderEnv;", start, end);
			LocalVariableNode renderEnv = ASMUtil.findLocalVariable(method, "renderEnv", "Lnet/optifine/render/RenderEnv;");
			method.instructions.insert(ASMUtil.first(method).varInsn("bufferBuilder", "Lnet/minecraft/client/renderer/BufferBuilder;").opcode(Opcodes.ASTORE).find(), ASMUtil.listOf(
					start,
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "bufferBuilder", "Lnet/minecraft/client/renderer/BufferBuilder;").index),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "blockState", "Lnet/minecraft/block/state/IBlockState;").index),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "pos", "Lnet/minecraft/util/math/BlockPos;").index),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/BufferBuilder", "getRenderEnv", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/optifine/render/RenderEnv;", false),
					new InsnNode(Opcodes.DUP),
					new VarInsnNode(Opcodes.ASTORE, renderEnv.index),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "bufferBuilderPack", "Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;").index),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/optifine/render/RenderEnv", "setRegionRenderCacheBuilder", "(Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)V", false)));
			method.instructions.insertBefore(ASMUtil.first(method).methodInsnObf("renderBlock", "func_175018_a").findThenNext().type(LabelNode.class).find(), ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, renderEnv.index),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/optifine/render/RenderEnv", "isOverlaysRendered", "()Z", false),
					new JumpInsnNode(Opcodes.IFEQ, end),
					new VarInsnNode(Opcodes.ALOAD, renderEnv.index),
					new InsnNode(Opcodes.ICONST_0),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/optifine/render/RenderEnv", "setOverlaysRendered", "(Z)V", false),
					end));
		});
		registry.add("meldexun.nothirium.mc.renderer.chunk.SectionRenderCache", "calculateCombinedLight", ClassWriter.COMPUTE_FRAMES, method -> {
			method.instructions.insert(ASMUtil.last(method).opcode(Opcodes.IRETURN).findThenPrev().type(LabelNode.class).find(), ASMUtil.listWithLabel(label -> ASMUtil.listOf(
					new MethodInsnNode(Opcodes.INVOKESTATIC, "Config", "isDynamicLights", "()Z", false),
					new JumpInsnNode(Opcodes.IFEQ, label),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "state", "Lnet/minecraft/block/state/IBlockState;").index),
					NothiriumClassTransformer.createObfMethodInsn(Opcodes.INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", "func_185914_p", "()Z", true),
					new JumpInsnNode(Opcodes.IFNE, label),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "pos", "Lnet/minecraft/util/math/BlockPos;").index),
					new VarInsnNode(Opcodes.ILOAD, ASMUtil.findLocalVariable(method, "light", "I").index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "net/optifine/DynamicLights", "getCombinedLight", "(Lnet/minecraft/util/math/BlockPos;I)I", false),
					new VarInsnNode(Opcodes.ISTORE, ASMUtil.findLocalVariable(method, "light", "I").index),
					label)));
		});
	}

}
