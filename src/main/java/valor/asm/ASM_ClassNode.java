package valor.asm;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ASM_ClassNode {
   public String name;
   public List<ASM_FieldNode> fields = new ArrayList<ASM_FieldNode>();
   public List<ASM_MethodNode> methods = new ArrayList<ASM_MethodNode>();
   public ClassNode originalNode;

   public void accept(ASM_ClassWriter cv) {
      if (this.originalNode != null) {
         for (ASM_MethodNode amn : this.methods) {
            amn.syncToOriginal();
         }
         try {
            SafeClassWriter cw = new SafeClassWriter(cv.getFlags());
            this.originalNode.accept(cw);
            cv.setBytes(cw.toByteArray());
         } catch (Throwable t) {
            System.err.println("[Valor] SafeClassWriter COMPUTE_FRAMES failed for " + this.name + ": " + t.getMessage());
            SafeClassWriter cw = new SafeClassWriter(ClassWriter.COMPUTE_MAXS);
            this.originalNode.accept(cw);
            cv.setBytes(cw.toByteArray());
         }
      }
   }
}
