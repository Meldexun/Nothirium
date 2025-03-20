package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;
import meldexun.asmutil2.MethodNodeTransformer;

public class BetterFoliageTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile", MethodNodeTransformer.builder("renderBlockState").priority(1000).build(method -> {
			MethodInsnNode canRenderInLayer = ASMUtil.first(method).methodInsn("canRenderInLayer").find();
			canRenderInLayer.setOpcode(Opcodes.INVOKESTATIC);
			canRenderInLayer.owner = "mods/betterfoliage/client/Hooks";
			canRenderInLayer.name = "canRenderBlockInLayer";
			canRenderInLayer.desc = "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockRenderLayer;)Z";

			MethodInsnNode renderBlock = ASMUtil.first(method).methodInsnObf("renderBlock", "func_175018_a").find();
			renderBlock.setOpcode(Opcodes.INVOKESTATIC);
			renderBlock.owner = "mods/betterfoliage/client/Hooks";
			renderBlock.name = "renderWorldBlock";
			renderBlock.desc = "(Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/util/BlockRenderLayer;)Z";
			method.instructions.insertBefore(renderBlock, new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "layer", "Lnet/minecraft/util/BlockRenderLayer;").index));
		}));
	}

}
