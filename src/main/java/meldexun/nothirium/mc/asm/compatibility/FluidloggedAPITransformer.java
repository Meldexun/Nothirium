package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;

public class FluidloggedAPITransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile", "compileSection", "(Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Lmeldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult;", 0, method -> {
			method.instructions.insert(ASMUtil.first(method).methodInsn("renderBlockState").find(), ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, 0),
					new VarInsnNode(Opcodes.ALOAD, 0),
					new FieldInsnNode(Opcodes.GETFIELD, "meldexun/nothirium/mc/renderer/chunk/RenderChunkTaskCompile", "chunkCache", "Lnet/minecraft/world/IBlockAccess;"),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "pos").index),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "blockState").index),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "visibilityGraph").index),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "bufferBuilderPack").index),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/FluidloggedAPI", "renderFluidState", "(Lmeldexun/nothirium/mc/renderer/chunk/RenderChunkTaskCompile;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lmeldexun/nothirium/util/VisibilityGraph;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)V", false)));
		});
	}

}
