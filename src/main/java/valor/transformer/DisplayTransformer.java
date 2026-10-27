package valor.transformer;

import valor.agent.BaseTransformer;

public class DisplayTransformer extends BaseTransformer {
   @Override
   public byte[] doTransform(byte[] var1) {

      return var1;
   }

   public DisplayTransformer() {
      super("org.lwjgl.opengl.Display");
   }
}
