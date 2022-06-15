package meldexun.nothirium.api.renderer;

public interface IVBOPart {

	int getVBO();

	int getFirst();

	int getCount();

	int getOffset();

	int getSize();

	void free();

	boolean isValid();

}
