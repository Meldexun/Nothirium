package meldexun.nothirium.util;

public class SectorizedList {

	private int sectorCount;
	private Sector firstSector;
	private Sector lastSector;
	private final FreeSectorManager freeSectors = new FreeSectorManager.RB();

	public SectorizedList(int sectorCount) {
		this.sectorCount = sectorCount;
		this.firstSector = new Sector(false, 0, sectorCount);
		this.lastSector = this.firstSector;
		this.freeSectors.add(this.firstSector);
	}

	protected void ensureCapacity(int minContinousSector) {
		if (minContinousSector <= 0) {
			throw new IllegalArgumentException();
		}
		if (this.freeSectors.largestSector() < minContinousSector) {
			this.grow(minContinousSector);
		}
	}

	protected void grow(int minContinousSector) {
		int oldCapacity = this.sectorCount;
		int lastFreeSectorStart = !this.lastSector.claimed ? this.lastSector.firstSector : oldCapacity;
		this.sectorCount = calculateNewCapacity(oldCapacity, lastFreeSectorStart, minContinousSector);

		// update prev and next sectors and freeSectors list
		if (!this.lastSector.claimed) {
			this.freeSectors.siftUp(this.lastSector, s -> {
				s.sectorCount += this.sectorCount - oldCapacity;
			});
		} else {
			Sector sector = new Sector(false, oldCapacity, this.sectorCount - oldCapacity);
			this.lastSector.next = sector;
			sector.prev = this.lastSector;
			this.lastSector = sector;
			this.freeSectors.add(sector);
		}
	}

	protected int calculateNewCapacity(int oldCapacity, int lastFreeSectorStart, int minContinousSector) {
		int newCapacity = oldCapacity;
		while (newCapacity - lastFreeSectorStart < minContinousSector) {
			newCapacity += newCapacity >> 1;
		}
		return newCapacity;
	}

	public Sector claim(int sectorCount) {
		if (sectorCount <= 0)
			throw new IllegalArgumentException();

		this.ensureCapacity(sectorCount);

		Sector freeSector = freeSectors.get(sectorCount);
		Sector sector = new Sector(true, freeSector.firstSector, sectorCount);

		// update prev and next sectors and freeSectors list
		Sector prev = freeSector.prev;
		Sector next = freeSector.next;
		if (freeSector.sectorCount == sectorCount) {
			this.freeSectors.remove(freeSector);

			if (prev != null) {
				sector.prev = prev;
				prev.next = sector;
			} else {
				this.firstSector = sector;
			}
			if (next != null) {
				sector.next = next;
				next.prev = sector;
			} else {
				this.lastSector = sector;
			}
		} else {
			this.freeSectors.siftDown(freeSector, s -> {
				s.firstSector += sectorCount;
				s.sectorCount -= sectorCount;
			});

			if (prev != null) {
				sector.prev = prev;
				prev.next = sector;
			} else {
				this.firstSector = sector;
			}
			sector.next = freeSector;
			freeSector.prev = sector;
		}

		return sector;
	}

	public void free(Sector sector) {
		if (!sector.claimed)
			throw new IllegalArgumentException();

		sector.claimed = false;

		// update prev and next sectors and freeSectors list
		Sector prev = sector.prev;
		Sector next = sector.next;
		boolean prevFree = prev != null && !prev.claimed;
		boolean nextFree = next != null && !next.claimed;
		if (prevFree) {
			if (nextFree) {
				// prev free - next free
				this.freeSectors.remove(next);
				this.freeSectors.siftUp(prev, s -> {
					s.sectorCount += sector.sectorCount;
					s.sectorCount += next.sectorCount;
				});
				prev.next = next.next;
				if (next.next != null) {
					next.next.prev = prev;
				} else {
					this.lastSector = prev;
				}
			} else {
				// prev free - next null/claimed
				this.freeSectors.siftUp(prev, s -> {
					s.sectorCount += sector.sectorCount;
				});
				prev.next = next;
				if (next != null) {
					next.prev = prev;
				} else {
					this.lastSector = prev;
				}
			}
		} else {
			if (nextFree) {
				// prev null/claimed - next free
				this.freeSectors.siftUp(next, s -> {
					s.firstSector = sector.firstSector;
					s.sectorCount += sector.sectorCount;
				});
				next.prev = prev;
				if (prev != null) {
					prev.next = next;
				} else {
					this.firstSector = next;
				}
			} else {
				// prev null/claimed - next null/claimed
				this.freeSectors.add(sector);
			}
		}
	}

	public int getSectorCount() {
		return this.sectorCount;
	}

	public static class Sector implements Comparable<Sector> {

		private boolean claimed;
		private int firstSector;
		private int sectorCount;
		private Sector prev;
		private Sector next;

		private Sector(boolean claimed, int firstSector, int sectorCount) {
			this.claimed = claimed;
			this.firstSector = firstSector;
			this.sectorCount = sectorCount;
		}

		@Override
		public String toString() {
			return String.format("%s(%d -> %d)", this.claimed ? "Claimed" : "Free", this.firstSector, this.firstSector + this.sectorCount - 1);
		}

		@Override
		public int compareTo(Sector o) {
			if (sectorCount < o.sectorCount)
				return -1;
			if (sectorCount > o.sectorCount)
				return 1;
			if (firstSector < o.firstSector)
				return -1;
			if (firstSector > o.firstSector)
				return 1;
			return 0;
		}

		public int getFirstSector() {
			return this.firstSector;
		}

		public int getSectorCount() {
			return this.sectorCount;
		}

	}

}
