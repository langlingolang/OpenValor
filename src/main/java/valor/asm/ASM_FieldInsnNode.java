package valor.asm;

public final class ASM_FieldInsnNode extends ASM_AbstractInsnNode {
   public String owner;
   public String name;
   public String desc;

   public ASM_FieldInsnNode(int opcode, String owner, String name, String desc) {
      super(opcode);
      this.owner = owner;
      this.name = name;
      this.desc = desc;
   }
}
