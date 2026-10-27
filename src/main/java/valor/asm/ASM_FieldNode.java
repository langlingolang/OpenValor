package valor.asm;

import org.objectweb.asm.tree.FieldNode;

public class ASM_FieldNode {
   public int access;
   public String name;
   public String desc;
   public String signature;
   public Object value;
   public FieldNode asmNode;

   public ASM_FieldNode(int access, String name, String desc, String signature, Object value) {
      this.access = access;
      this.name = name;
      this.desc = desc;
      this.signature = signature;
      this.value = value;
   }
}
