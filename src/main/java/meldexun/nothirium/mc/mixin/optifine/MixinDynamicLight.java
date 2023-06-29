package meldexun.nothirium.mc.mixin.optifine;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.integration.Optifine;
import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.renderer.chunk.AbstractRenderChunk;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

@Pseudo
@Mixin(targets = "net.optifine.DynamicLight", remap = false)
public class MixinDynamicLight {

	@Shadow
	private Entity entity;
	@Shadow
	private double offsetY;
	@Shadow
	private double lastPosX;
	@Shadow
	private double lastPosY;
	@Shadow
	private double lastPosZ;
	@Shadow
	private int lastLightLevel;
	@Shadow
	private boolean underwater;
	@Shadow
	private long timeCheckMs;
	@Shadow
	private Set<BlockPos> setLitChunkPos;
	@Shadow
	private MutableBlockPos blockPosMutable;

	@Inject(method = "update", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void on_getWorldFromBlockAccess(RenderGlobal renderGlobal, CallbackInfo info) {
		info.cancel();

		if (Optifine.IS_DYNAMIC_LIGHTS_FAST.invoke(null)) {
			long timeNowMs = System.currentTimeMillis();
			if (timeNowMs < this.timeCheckMs + 500L) {
				return;
			}
			this.timeCheckMs = timeNowMs;
		}
		double posX = this.entity.posX - 0.5;
		double posY = this.entity.posY - 0.5 + this.offsetY;
		double posZ = this.entity.posZ - 0.5;
		int lightLevel = Optifine.GET_LIGHT_LEVEL.invoke(null, this.entity);
		double dx = posX - this.lastPosX;
		double dy = posY - this.lastPosY;
		double dz = posZ - this.lastPosZ;
		double delta = 0.1;
		if (dx * dx + dy * dy + dz * dz <= delta * delta && this.lastLightLevel == lightLevel) {
			return;
		}
		this.lastPosX = posX;
		this.lastPosY = posY;
		this.lastPosZ = posZ;
		this.lastLightLevel = lightLevel;
		World world = Minecraft.getMinecraft().world;
		if (world != null) {
			this.blockPosMutable.setPos(posX, posY, posZ);
			IBlockState state = world.getBlockState(this.blockPosMutable);
			this.underwater = state.getMaterial() == Material.WATER;
		} else {
			this.underwater = false;
		}
		Set<BlockPos> setNewPos = new HashSet<>();
		if (lightLevel > 0) {
			EnumFacing dirX = ((MathHelper.floor(posX) & 0xF) >= 8) ? EnumFacing.EAST : EnumFacing.WEST;
			EnumFacing dirY = ((MathHelper.floor(posY) & 0xF) >= 8) ? EnumFacing.UP : EnumFacing.DOWN;
			EnumFacing dirZ = ((MathHelper.floor(posZ) & 0xF) >= 8) ? EnumFacing.SOUTH : EnumFacing.NORTH;
			BlockPos chunkPos = new BlockPos(posX, posY, posZ);
			BlockPos chunkPosX = chunkPos.offset(dirX, 16);
			BlockPos chunkPosY = chunkPos.offset(dirY, 16);
			BlockPos chunkPosZ = chunkPos.offset(dirZ, 16);
			BlockPos chunkPosXY = chunkPosX.offset(dirY, 16);
			BlockPos chunkPosXZ = chunkPosX.offset(dirZ, 16);
			BlockPos chunkPosYZ = chunkPosY.offset(dirZ, 16);
			BlockPos chunkPosXYZ = chunkPosXY.offset(dirZ, 16);
			this.updateChunkLight(renderGlobal, chunkPos, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosX, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosY, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosZ, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosXY, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosXZ, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosYZ, this.setLitChunkPos, setNewPos);
			this.updateChunkLight(renderGlobal, chunkPosXYZ, this.setLitChunkPos, setNewPos);
		}
		this.updateLitChunks(renderGlobal);
		this.setLitChunkPos = setNewPos;
	}

	@Unique
	private void updateChunkLight(RenderGlobal renderGlobal, BlockPos pos, Set<BlockPos> setPrevPos,
			Set<BlockPos> setNewPos) {
		IRenderChunkProvider<?> provider = ChunkRenderManager.getProvider();
		IRenderChunk chunk = provider.getRenderChunkAt(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
		if (chunk != null) {
			((AbstractRenderChunk) chunk).markDirty();
		}

		if (setPrevPos != null) {
			setPrevPos.remove(pos);
		}
		if (setNewPos != null) {
			setNewPos.add(pos);
		}
	}

	@Unique
	private void updateLitChunks(RenderGlobal renderGlobal) {
		for (BlockPos posOld : this.setLitChunkPos) {
			this.updateChunkLight(renderGlobal, posOld, null, null);
		}
	}

	@Inject(method = "updateLitChunks", remap = false, require = 1, cancellable = true, at = @At("HEAD"))
	private void on_updateLitChunks(RenderGlobal renderGlobal, CallbackInfo info) {
		info.cancel();

		this.updateLitChunks(renderGlobal);
	}

}
