package meldexun.nothirium.util.function;

@FunctionalInterface
public interface IntIntIntObj2ObjFunction<T, R> {

	R apply(int x, int y, int z, T t);

}
