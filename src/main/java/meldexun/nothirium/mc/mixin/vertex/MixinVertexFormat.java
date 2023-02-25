package meldexun.nothirium.mc.mixin.vertex;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import meldexun.nothirium.mc.vertex.ExtendedVertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

@Mixin(VertexFormat.class)
public class MixinVertexFormat {

	@Shadow
	@Final
	private List<VertexFormatElement> elements;
	@Shadow
	@Final
	private List<Integer> offsets;
	@Shadow
	@Final
	private int vertexSize;

	@ModifyVariable(method = "addElement", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = Shift.BY, by = -2), index = 1, ordinal = 0, name = "element")
	private VertexFormatElement pre_addElement(VertexFormatElement element) {
		element = new VertexFormatElement(element.getIndex(), element.getType(), element.getUsage(),
				element.getElementCount());
		((ExtendedVertexFormatElement) element).setVertexFormat((VertexFormat) (Object) this);
		((ExtendedVertexFormatElement) element).setOffset(vertexSize);
		return element;
	}

	@ModifyVariable(method = "addElement", at = @At(value = "RETURN", ordinal = 1), index = 1, ordinal = 0, name = "element")
	private VertexFormatElement post_addElement(VertexFormatElement element) {
		if (elements.size() >= 2) {
			((ExtendedVertexFormatElement) elements.get(elements.size() - 2))
					.setNext(elements.get(elements.size() - 1));
		}
		((ExtendedVertexFormatElement) elements.get(elements.size() - 1)).setNext(elements.get(0));
		return element;
	}

}
