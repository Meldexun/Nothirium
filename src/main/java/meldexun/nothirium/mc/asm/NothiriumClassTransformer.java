package meldexun.nothirium.mc.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;

import meldexun.asmutil.ASMUtil;
import meldexun.asmutil.transformer.clazz.AbstractClassTransformer;
import net.minecraft.launchwrapper.IClassTransformer;

public class NothiriumClassTransformer extends AbstractClassTransformer implements IClassTransformer {

	public static final boolean OPTIFINE_DETECTED;
	static {
		boolean flag = false;
		try {
			Class.forName("optifine.OptiFineClassTransformer", false, NothiriumPlugin.class.getClassLoader());
			flag = true;
		} catch (ClassNotFoundException e) {
			// ignore
		}
		OPTIFINE_DETECTED = flag;
	}

	@Override
	protected void registerTransformers() {
		// @formatter:off
		this.registerMethodTransformer("buy", "a", "(Lbsb;)V", "net/minecraft/client/renderer/RenderGlobal", "setWorldAndLoadRenderers", "(Lnet/minecraft/client/multiplayer/WorldClient;)V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: setWorldAndLoadRenderers net/minecraft/client/renderer/RenderGlobal");

			AbstractInsnNode targetNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKEINTERFACE, "java/util/Set", "clear", "()V", "java/util/Set", "clear", "()V");
			targetNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode1);

			AbstractInsnNode popNode1 = ASMUtil.findLastFieldCall(methodNode, Opcodes.PUTFIELD, "buy", "N", "Lbxm;", "net/minecraft/client/renderer/RenderGlobal", "renderDispatcher", "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;");
			popNode1 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.LABEL, popNode1);

			methodNode.instructions.insert(targetNode1, ASMUtil.listOf(
				new MethodInsnNode(Opcodes.INVOKESTATIC, "meldexun/nothirium/mc/renderer/ChunkRenderManager", "dispose", "()V", false),
				new JumpInsnNode(Opcodes.GOTO, (LabelNode) popNode1)
			));
		});

		this.registerMethodTransformer("buy", "a", "()V", "net/minecraft/client/renderer/RenderGlobal", "loadRenderers", "()V", methodNode -> {
			ASMUtil.LOGGER.info("Transforming method: loadRenderers net/minecraft/client/renderer/RenderGlobal");

			AbstractInsnNode targetNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESPECIAL, "bxm", "<init>", "()V", "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher", "<init>", "()V");
			targetNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.JUMP_INSN, targetNode1);
			targetNode1 = ASMUtil.findLastInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode1);
			AbstractInsnNode popNode1 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESPECIAL, "bxm", "<init>", "()V", "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher", "<init>", "()V", targetNode1); 
			popNode1 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.LABEL, popNode1);

			AbstractInsnNode targetNode2 = ASMUtil.findFirstMethodCall(methodNode, Opcodes.INVOKESPECIAL, "buy", "q", "()V", "net/minecraft/client/renderer/RenderGlobal", "generateSky2", "()V", popNode1);
			targetNode2 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.LABEL, targetNode2);
			AbstractInsnNode popNode2 = ASMUtil.findLastFieldCall(methodNode, Opcodes.PUTFIELD, "buy", "Q", "I", "net/minecraft/client/renderer/RenderGlobal", "renderEntitiesStartupCounter", "I"); 
			popNode2 = ASMUtil.findFirstInsnByType(methodNode, AbstractInsnNode.LABEL, popNode2);

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

}
