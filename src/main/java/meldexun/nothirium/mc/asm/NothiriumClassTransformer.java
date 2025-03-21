package meldexun.nothirium.mc.asm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
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
import meldexun.nothirium.mc.asm.compatibility.ImmersivePetroleumTransformer;
import meldexun.nothirium.mc.asm.compatibility.MultiblockedTransformer;
import meldexun.nothirium.mc.asm.compatibility.OptifineTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class NothiriumClassTransformer extends HashMapClassNodeClassTransformer implements IClassTransformer {

	private static final ClassUtil REMAPPING_CLASS_UTIL;
	static {
		try {
			Class<?> FMLDeobfuscatingRemapper = Class.forName("net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper", true, Launch.classLoader);
			Field _INSTANCE = FMLDeobfuscatingRemapper.getField("INSTANCE");
			Field _classNameBiMap = FMLDeobfuscatingRemapper.getDeclaredField("classNameBiMap");
			_classNameBiMap.setAccessible(true);
			@SuppressWarnings("unchecked")
			BiMap<String, String> deobfuscationMap = (BiMap<String, String>) _classNameBiMap.get(_INSTANCE.get(null));
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
		// @formatter:on

		Map<Path, ZipFile> zipCache = new HashMap<>();
		Predicate<String> doesClassExist = className -> {
			className = className.replace('.', '/') + ".class";

			if (Launch.classLoader.getResource(className) != null) {
				return true;
			}

			try {
				String s = className;
				return Files.list(Paths.get("./mods"))
						.filter(Files::isRegularFile)
						.filter(p -> p.getFileName().toString().endsWith(".jar"))
						.map(p -> {
							if (zipCache.containsKey(p)) {
								return zipCache.get(p);
							}
							ZipFile zip;
							try {
								zip = new ZipFile(p.toFile());
							} catch (IOException e) {
								zip = null;
							}
							zipCache.put(p, zip);
							return zip;
						})
						.filter(Objects::nonNull)
						.map(zip -> zip.getEntry(s))
						.anyMatch(Objects::nonNull);
			} catch (IOException e) {
				// ignore
			}

			return false;
		};
		if (doesClassExist.test("mods.betterfoliage.loader.BetterFoliageLoader")) {
			BetterFoliageTransformer.registerTransformers(registry);
		}
		if (doesClassExist.test("lumien.chunkanimator.asm.LoadingPlugin")) {
			ChunkAnimatorTransformer.registerTransformers(registry);
		}
		if (doesClassExist.test("io.github.opencubicchunks.cubicchunks.core.asm.coremod.CubicChunksCoreMod")) {
			CubicChunksTransformer.registerTransformers(registry);
		}
		if (doesClassExist.test("git.jbredwards.fluidlogged_api.mod.asm.ASMHandler")) {
			FluidloggedAPITransformer.registerTransformers(registry);
		}
		if (doesClassExist.test("flaxbeard.immersivepetroleum.ImmersivePetroleum")) {
			ImmersivePetroleumTransformer.registerTransformers(registry);
		}
		if (doesClassExist.test("com.cleanroommc.multiblocked.core.MultiblockedLoadingPlugin")) {
			MultiblockedTransformer.registerTransformers(registry);
		}
		if (doesClassExist.test("optifine.OptiFineClassTransformer")) {
			OptifineTransformer.registerTransformers(registry);
		}
		IOException e = null;
		for (ZipFile zip : zipCache.values()) {
			try {
				zip.close();
			} catch (IOException e1) {
				if (e == null) {
					e = e1;
				} else {
					e.addSuppressed(e1);
				}
			}
		}
		if (e != null) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	protected ClassWriter createClassWriter(int flags) {
		return new NonLoadingClassWriter(flags, REMAPPING_CLASS_UTIL);
	}

	public static FieldInsnNode createObfFieldInsn(int opcode, String owner, String name, String desc) {
		return new FieldInsnNode(opcode, owner, FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, name, desc), desc);
	}

	public static MethodInsnNode createObfMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		return new MethodInsnNode(opcode, owner, FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, name, desc), desc, itf);
	}

}
