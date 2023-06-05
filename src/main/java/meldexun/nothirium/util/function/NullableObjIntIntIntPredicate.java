package meldexun.nothirium.util.function;

import javax.annotation.Nullable;

public interface NullableObjIntIntIntPredicate<T> {

	boolean test(@Nullable T t, int x, int y, int z);

}
