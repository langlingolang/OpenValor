package valor.asm;

import org.objectweb.asm.ClassWriter;

public class ASM_ClassWriter extends ASM_BaseClassWriter {
   private final int flags;

   public ASM_ClassWriter() {
      this(ClassWriter.COMPUTE_FRAMES);
   }

   public ASM_ClassWriter(int flags) {
      this.flags = flags;
   }

   public int getFlags() {
      return this.flags;
   }

   public void setBytes(byte[] bytes) {
      this.bytes = bytes;
   }
}
