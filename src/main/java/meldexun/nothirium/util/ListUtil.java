package meldexun.nothirium.util;

import java.util.List;
import java.util.function.ObjIntConsumer;

public class ListUtil {

	public static <T> void forEach(List<T> list, boolean reversed, ObjIntConsumer<T> action) {
		if (!reversed) {
			for (int i = 0; i < list.size(); i++) {
				action.accept(list.get(i), i);
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				action.accept(list.get(list.size() - 1 - i), i);
			}
		}
	}

}
