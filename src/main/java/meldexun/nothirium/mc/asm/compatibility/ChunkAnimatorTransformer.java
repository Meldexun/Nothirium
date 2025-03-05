package meldexun.nothirium.mc.asm.compatibility;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import meldexun.asmutil2.ASMUtil;
import meldexun.asmutil2.IClassTransformerRegistry;

public class ChunkAnimatorTransformer {

	public static void registerTransformers(IClassTransformerRegistry registry) {
		registry.add("meldexun.nothirium.renderer.chunk.AbstractRenderChunk", "<init>", 0, method -> {
			method.instructions.insert(ASMUtil.first(method).methodInsn("markDirty").find(), ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/ChunkAnimator", "onSetCoords", "(Lmeldexun/nothirium/api/renderer/chunk/IRenderChunk;)V", false)));
		});
		registry.add("meldexun.nothirium.renderer.chunk.AbstractRenderChunk", "setCoords", 0, method -> {
			method.instructions.insert(ASMUtil.first(method).methodInsn("markDirty").find(), ASMUtil.listOf(
					new VarInsnNode(Opcodes.ALOAD, 0),
					new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/ChunkAnimator", "onSetCoords", "(Lmeldexun/nothirium/api/renderer/chunk/IRenderChunk;)V", false)));
		});

		registry.add("meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL15", "draw", ClassWriter.COMPUTE_FRAMES, ChunkAnimatorTransformer::applyOffset);
		registry.add("meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL20", "draw", ClassWriter.COMPUTE_FRAMES, ChunkAnimatorTransformer::applyOffset);
		registry.add("meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL42", "record", ClassWriter.COMPUTE_FRAMES, ChunkAnimatorTransformer::applyOffset);
		registry.add("meldexun.nothirium.mc.renderer.chunk.ChunkRendererGL43", "record", ClassWriter.COMPUTE_FRAMES, ChunkAnimatorTransformer::applyOffset);
	}

	private static void applyOffset(MethodNode method) {
		LabelNode start = new LabelNode();
		LabelNode end = new LabelNode();
		ASMUtil.addLocalVariable(method, "offset", "Lorg/lwjgl/util/vector/Vector3f;", start, end);
		LocalVariableNode offset = ASMUtil.findLocalVariable(method, "offset", "Lorg/lwjgl/util/vector/Vector3f;");
		LocalVariableNode renderChunk = ASMUtil.findLocalVariable(method, "renderChunk", "Lmeldexun/nothirium/mc/renderer/chunk/RenderChunk;");
		LocalVariableNode cameraX = ASMUtil.findLocalVariable(method, "cameraX", "D");
		LocalVariableNode cameraY = ASMUtil.findLocalVariable(method, "cameraY", "D");
		LocalVariableNode cameraZ = ASMUtil.findLocalVariable(method, "cameraZ", "D");

		method.instructions.insert(ASMUtil.listOf(
				start,
				new VarInsnNode(Opcodes.ALOAD, renderChunk.index),
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/integration/ChunkAnimator", "getOffset", "(Lmeldexun/nothirium/api/renderer/chunk/IRenderChunk;)Lorg/lwjgl/util/vector/Vector3f;", false),
				new InsnNode(Opcodes.DUP),
				new VarInsnNode(Opcodes.ASTORE, offset.index),
				new JumpInsnNode(Opcodes.IFNULL, end),

				new VarInsnNode(Opcodes.DLOAD, cameraX.index),
				new VarInsnNode(Opcodes.ALOAD, offset.index),
				new FieldInsnNode(Opcodes.GETFIELD, "org/lwjgl/util/vector/Vector3f", "x", "F"),
				new InsnNode(Opcodes.F2D),
				new InsnNode(Opcodes.DSUB),
				new VarInsnNode(Opcodes.DSTORE, cameraX.index),

				new VarInsnNode(Opcodes.DLOAD, cameraY.index),
				new VarInsnNode(Opcodes.ALOAD, offset.index),
				new FieldInsnNode(Opcodes.GETFIELD, "org/lwjgl/util/vector/Vector3f", "y", "F"),
				new InsnNode(Opcodes.F2D),
				new InsnNode(Opcodes.DSUB),
				new VarInsnNode(Opcodes.DSTORE, cameraY.index),

				new VarInsnNode(Opcodes.DLOAD, cameraZ.index),
				new VarInsnNode(Opcodes.ALOAD, offset.index),
				new FieldInsnNode(Opcodes.GETFIELD, "org/lwjgl/util/vector/Vector3f", "z", "F"),
				new InsnNode(Opcodes.F2D),
				new InsnNode(Opcodes.DSUB),
				new VarInsnNode(Opcodes.DSTORE, cameraZ.index),

				end));
	}

}
