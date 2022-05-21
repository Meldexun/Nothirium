package meldexun.nothirium.api.renderer;

public interface IVBOPart {

	int getVBO();

	int getFirst();

	int getCount();

	void free();

	boolean isValid();

}
