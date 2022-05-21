package meldexun.nothirium.api.renderer.chunk;

public enum ChunkRenderPass {

	// In 1.12 cutout comes before cutoutMipped and tripwire does not exist!
	SOLID,
	CUTOUT,
	CUTOUT_MIPPED,
	TRANSLUCENT;

	public static final ChunkRenderPass[] ALL = ChunkRenderPass.values();

}
