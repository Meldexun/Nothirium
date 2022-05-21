package meldexun.nothirium.util;

public class VisibilitySet {

	private long visibilities;

	public void setVisible(Direction origin, Direction dir) {
		visibilities |= 1L << (origin.ordinal() * 6) << dir.ordinal();
		visibilities |= 1L << (dir.ordinal() * 6) << origin.ordinal();
	}

	public boolean isVisible(Direction origin, Direction dir) {
		return ((int) (visibilities >>> (origin.ordinal() * 6)) & (1 << dir.ordinal())) != 0;
	}

	public boolean allVisible() {
		return visibilities == 0xF_FFFF_FFFFL;
	}

	public int allVisibleFrom(Direction origin) {
		return (int) (visibilities >>> (origin.ordinal() * 6)) & 0x3F;
	}

	public void setAllVisible() {
		this.visibilities = 0xF_FFFF_FFFFL;
	}

}
