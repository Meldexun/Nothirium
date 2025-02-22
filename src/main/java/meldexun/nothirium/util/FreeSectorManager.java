package meldexun.nothirium.util;

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import meldexun.nothirium.util.SectorizedList.Sector;
import meldexun.reflectionutil.ReflectionField;
import meldexun.reflectionutil.ReflectionMethod;

public interface FreeSectorManager {

	int largestSector();

	Sector get(int minSectorSize);

	void add(Sector sector);

	void remove(Sector sector);

	default void siftUp(Sector sector, Consumer<Sector> c) {
		remove(sector);
		c.accept(sector);
		add(sector);
	}

	default void siftDown(Sector sector, Consumer<Sector> c) {
		remove(sector);
		c.accept(sector);
		add(sector);
	}

	abstract class Map<T extends Object2ObjectSortedMap<Sector, Sector>> implements FreeSectorManager {

		protected final T map;

		public Map(T map) {
			this.map = map;
		}

		@Override
		public void add(Sector sector) {
			map.put(sector, sector);
		}

		@Override
		public int largestSector() {
			return map.isEmpty() ? 0 : map.lastKey().getSectorCount();
		}

		@Override
		public void remove(Sector sector) {
			map.remove(sector);
		}

	}

	class AVL extends Map<Object2ObjectAVLTreeMap<Sector, Sector>> {

		private static final ReflectionField<?> TREE = new ReflectionField<>(Object2ObjectAVLTreeMap.class, "tree", "tree");
		private static final ReflectionMethod<?> LEFT = new ReflectionMethod<>(Object2ObjectAVLTreeMap.class.getName() + "$Entry", "left", "left");
		private static final ReflectionMethod<?> RIGHT = new ReflectionMethod<>(Object2ObjectAVLTreeMap.class.getName() + "$Entry", "right", "right");

		public AVL(Object2ObjectAVLTreeMap<Sector, Sector> map) {
			super(map);
		}

		@Override
		public Sector get(int minSectorSize) {
			if (largestSector() < minSectorSize)
				return null;
			Sector q = null;
			Object p = TREE.get(map);
			while (p != null) {
				@SuppressWarnings("unchecked")
				Sector s = ((Object2ObjectMap.Entry<Sector, Sector>) p).getKey();
				if (s.getSectorCount() < minSectorSize) {
					p = RIGHT.invoke(p);
					continue;
				}
				if (s.getSectorCount() > minSectorSize) {
					q = s;
					p = LEFT.invoke(p);
					continue;
				}
				return s;
			}
			return q;
		}

	}

	class RB extends Map<Object2ObjectRBTreeMap<Sector, Sector>> {

		private static final ReflectionField<?> TREE = new ReflectionField<>(Object2ObjectRBTreeMap.class, "tree", "tree");
		private static final ReflectionMethod<?> LEFT = new ReflectionMethod<>(Object2ObjectRBTreeMap.class.getName() + "$Entry", "left", "left");
		private static final ReflectionMethod<?> RIGHT = new ReflectionMethod<>(Object2ObjectRBTreeMap.class.getName() + "$Entry", "right", "right");

		public RB() {
			super(new Object2ObjectRBTreeMap<>());
		}

		@Override
		public Sector get(int minSectorSize) {
			if (largestSector() < minSectorSize)
				return null;
			Sector q = null;
			Object p = TREE.get(map);
			while (p != null) {
				@SuppressWarnings("unchecked")
				Sector s = ((Object2ObjectMap.Entry<Sector, Sector>) p).getKey();
				if (s.getSectorCount() < minSectorSize) {
					p = RIGHT.invoke(p);
					continue;
				}
				if (s.getSectorCount() > minSectorSize) {
					q = s;
					p = LEFT.invoke(p);
					continue;
				}
				return s;
			}
			return q;
		}

	}

}
