package valor.asm;

public final class ASM_MethodInsnNode extends ASM_AbstractInsnNode {
   public String owner;
   public String name;
   public String desc;
   public boolean itf;

   public final void setStatic() {
      super.opcode = 184;
   }

   public ASM_MethodInsnNode(int opcode, String owner, String name, String desc, boolean itf) {
      super(opcode);
      this.owner = owner;
      this.name = name;
      this.desc = desc;
      this.itf = itf;
   }
}
