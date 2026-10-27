package valor.transformer;

import valor.agent.BaseTransformer;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;

public class RendererLivingEntityTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 8);
      return var1;
   }

   public RendererLivingEntityTransformer() {
      super("net.minecraft.client.renderer.entity.RendererLivingEntity");
   }
}
