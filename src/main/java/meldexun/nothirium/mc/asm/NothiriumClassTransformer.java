package meldexun.nothirium.mc.asm;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
import meldexun.nothirium.mc.asm.compatibility.BetterFoliageTransformer;
import meldexun.nothirium.mc.asm.compatibility.ChunkAnimatorTransformer;
import meldexun.nothirium.mc.asm.compatibility.CubicChunksTransformer;
import meldexun.nothirium.mc.asm.compatibility.FluidloggedAPITransformer;
import meldexun.nothirium.mc.asm.compatibility.MultiblockedTransformer;
import meldexun.nothirium.mc.asm.compatibility.OptifineTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

public class NothiriumClassTransformer extends HashMapClassNodeClassTransformer implements IClassTransformer {

	private static final ClassUtil REMAPPING_CLASS_UTIL;
	static {
		try {
			Field _classLoader = FMLDeobfuscatingRemapper.class.getDeclaredField("classLoader");
			_classLoader.setAccessible(true);
			if (_classLoader.get(FMLDeobfuscatingRemapper.INSTANCE) == null) {
				Method _debfuscationDataName = FMLInjectionData.class.getDeclaredMethod("debfuscationDataName");
				_debfuscationDataName.setAccessible(true);
				Map<String, Object> callData = new HashMap<String, Object>();
				callData.put("runtimeDeobfuscationEnabled", false);
				callData.put("mcLocation", Launch.minecraftHome);
				callData.put("classLoader", Launch.classLoader);
				callData.put("deobfuscationFileName", _debfuscationDataName.invoke(null));
				new FMLSanityChecker().injectData(callData);
			}

			Field _classNameBiMap = FMLDeobfuscatingRemapper.class.getDeclaredField("classNameBiMap");
			_classNameBiMap.setAccessible(true);
			@SuppressWarnings("unchecked")
			BiMap<String, String> deobfuscationMap = (BiMap<String, String>) _classNameBiMap.get(FMLDeobfuscatingRemapper.INSTANCE);
			REMAPPING_CLASS_UTIL = ClassUtil.getInstance(new ClassUtil.Configuration(Launch.classLoader, deobfuscationMap.inverse(), deobfuscationMap));
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

		if (doesClassExist("mods.betterfoliage.loader.BetterFoliageLoader")) {
			BetterFoliageTransformer.registerTransformers(registry);
		}
		if (doesClassExist("lumien.chunkanimator.asm.LoadingPlugin")) {
			ChunkAnimatorTransformer.registerTransformers(registry);
		}
		if (doesClassExist("io.github.opencubicchunks.cubicchunks.core.asm.coremod.CubicChunksCoreMod")) {
			CubicChunksTransformer.registerTransformers(registry);
		}
		if (doesClassExist("git.jbredwards.fluidlogged_api.mod.asm.ASMHandler")) {
			FluidloggedAPITransformer.registerTransformers(registry);
		}
		if (doesClassExist("com.cleanroommc.multiblocked.core.MultiblockedLoadingPlugin")) {
			MultiblockedTransformer.registerTransformers(registry);
		}
		if (doesClassExist("optifine.OptiFineClassTransformer")) {
			OptifineTransformer.registerTransformers(registry);
		}
		// @formatter:on
	}

	private static boolean doesClassExist(String className) {
		try {
			Class.forName(className, false, NothiriumClassTransformer.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	@Override
	protected ClassWriter createClassWriter(int flags) {
		return new NonLoadingClassWriter(flags, REMAPPING_CLASS_UTIL);
	}

}
