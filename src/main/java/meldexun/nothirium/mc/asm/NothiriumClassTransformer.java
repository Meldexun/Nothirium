package meldexun.nothirium.mc.asm;

import java.lang.reflect.Field;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import com.google.common.collect.BiMap;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.HashMapClassNodeClassTransformer;
import meldexun.asmutil2.IClassTransformerRegistry;
import meldexun.asmutil2.NonLoadingClassWriter;
import meldexun.asmutil2.reader.ClassUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class NothiriumClassTransformer extends HashMapClassNodeClassTransformer implements IClassTransformer {

	private static final ClassUtil.Configuration REMAPPING_CONFIGURATION;
	static {
		try {
			Field f = FMLDeobfuscatingRemapper.class.getDeclaredField("classNameBiMap");
			f.setAccessible(true);
			@SuppressWarnings("unchecked")
			BiMap<String, String> classNameBiMap = (BiMap<String, String>) f.get(FMLDeobfuscatingRemapper.INSTANCE);
			REMAPPING_CONFIGURATION = new ClassUtil.Configuration(NothiriumClassTransformer.class.getClassLoader(), classNameBiMap.inverse(), classNameBiMap);
		} catch (ReflectiveOperationException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	@Override
	protected void registerTransformers(IClassTransformerRegistry registry) {
		// @formatter:off
		registry.addObf("net.minecraft.client.renderer.RenderGlobal", "setWorldAndLoadRenderers", "func_72732_a", "(Lnet/minecraft/client/multiplayer/WorldClient;)V", ClassWriter.COMPUTE_FRAMES, methodNode -> {
			AbstractInsnNode targetNode1 = ASMUtil.first(methodNode).opcode(Opcodes.INVOKEINTERFACE).methodInsn("java/util/Set", "clear", "()V").find();
			targetNode1 = ASMUtil.prev(methodNode, targetNode1).type(LabelNode.class).find();

			AbstractInsnNode popNode1 = ASMUtil.last(methodNode).opcode(Opcodes.PUTFIELD).fieldInsnObf("net/minecraft/client/renderer/RenderGlobal", "renderDispatcher", "field_174995_M", "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;").find();
			popNode1 = ASMUtil.next(methodNode, popNode1).type(LabelNode.class).find();

			methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/renderer/ChunkRenderManager", "dispose", "()V", false),
				new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode1)
			));
		});

		registry.addObf("net.minecraft.client.renderer.RenderGlobal", "loadRenderers", "func_72712_a", "()V", ClassWriter.COMPUTE_FRAMES, methodNode -> {
			AbstractInsnNode targetNode1 = ASMUtil.first(methodNode).opcode(Opcodes.INVOKESPECIAL).methodInsn("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher", "<init>", "()V").find();
			targetNode1 = ASMUtil.prev(methodNode, targetNode1).type(JumpInsnNode.class).find();
			targetNode1 = ASMUtil.prev(methodNode, targetNode1).type(LabelNode.class).find();
			AbstractInsnNode popNode1 = ASMUtil.next(methodNode, targetNode1).opcode(Opcodes.INVOKESPECIAL).methodInsn("net/minecraft/client/renderer/chunk/ChunkRenderDispatcher", "<init>", "()V").find(); 
			popNode1 = ASMUtil.next(methodNode, popNode1).type(LabelNode.class).find();

			AbstractInsnNode targetNode2 = ASMUtil.next(methodNode, popNode1).methodInsnObf("net/minecraft/client/renderer/RenderGlobal", "generateSky2", "func_174964_o", "()V").find();
			targetNode2 = ASMUtil.next(methodNode, targetNode2).type(LabelNode.class).find();
			AbstractInsnNode popNode2 = ASMUtil.last(methodNode).opcode(Opcodes.PUTFIELD).fieldInsnObf("net/minecraft/client/renderer/RenderGlobal", "renderEntitiesStartupCounter", "field_72740_G", "I").find(); 
			popNode2 = ASMUtil.next(methodNode, popNode2).type(LabelNode.class).find();

			methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
				new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode1)
			));

			methodNode.instructions.insert(targetNode2, ASMUtil.listOf(
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/renderer/ChunkRenderManager", "allChanged", "()V", false),
				new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode2)
			));
		});
		// @formatter:on
	}

	@Override
	protected ClassWriter createClassWriter(int flags) {
		return new NonLoadingClassWriter(flags) {
			@Override
			protected ClassUtil.Configuration getClassUtilConfiguration() {
				return REMAPPING_CONFIGURATION;
			}
		};
	}

}
