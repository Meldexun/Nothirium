package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;
import meldexun.nothirium.mc.asm.NothiriumClassTransformer;

public class ImmersivePetroleumTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.mc.renderer.chunk.RenderChunkTaskCompile", "renderBlockState", 0, method -> {
			method.instructions.insertBefore(ASMUtil.first(method).fieldInsn("ALL", "[Lnet/minecraft/util/BlockRenderLayer;").find(), ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "blockState", "Lnet/minecraft/block/state/IBlockState;").index),
					NothiriumClassTransformer.createObfMethodInsn(Opcodes.INVOKEINTERFACE, "net/minecraft/block/state/IBlockState", "func_177230_c", "()Lnet/minecraft/block/Block;", true),
					new VarInsnNode(Opcodes.ALOAD, ASMUtil.findLocalVariable(method, "blockState", "Lnet/minecraft/block/state/IBlockState;").index),
					new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/block/Block", "hasTileEntity", "(Lnet/minecraft/block/state/IBlockState;)Z", false),
					new InsnNode(Opcodes.POP)));
		});
	}

}
