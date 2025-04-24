package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;

public class SecretRoomsTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile", "compileSection", "(Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Lmeldexun/nothirium/api/renderer/chunk/RenderChunkTaskResult;", 0, method -> {
			MethodInsnNode getBlockState = ASMUtil.first(method).methodInsnObf("getBlockState", "func_180495_p").find();
			getBlockState.setOpcode(Opcodes.INVOKESTATIC);
			getBlockState.owner = "com/wynprice/secretroomsmod/core/SecretRoomsHooksClient";
			getBlockState.name = "getBlockState";
			getBlockState.desc = "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;";
			getBlockState.itf = false;
		});
	}

}
