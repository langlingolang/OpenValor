package valor.transformer;

import java.util.Iterator;
import valor.agent.BaseTransformer;
import valor.asm.ASM_AbstractInsnNode;
import valor.asm.ASM_ClassNode;
import valor.asm.ASM_ClassReader;
import valor.asm.ASM_FieldInsnNode;
import valor.asm.ASM_FieldNode;
import valor.asm.ASM_InsnList;
import valor.asm.ASM_InsnNode;
import valor.asm.ASM_LdcInsnNode;
import valor.asm.ASM_MethodInsnNode;
import valor.asm.ASM_MethodNode;
import valor.asm.ASM_VarInsnNode;

public class EntityRendererTransformer extends BaseTransformer {
   private String mcFieldName;

   @Override
   public byte[] doTransform(byte[] var1) {
      ASM_ClassReader var2 = new ASM_ClassReader(var1);
      ASM_ClassNode var3 = new ASM_ClassNode();
      var2.accept(var3, 4);
      String var10000;
      if (this.mcFieldName != null) {
         var10000 = this.mcFieldName;
      } else {
         Iterator var4 = var3.fields.iterator();

         while (true) {
            if (!var4.hasNext()) {
               this.mcFieldName = "mc";
               var10000 = this.mcFieldName;
               break;
            }

            ASM_FieldNode var5;
            if ((var5 = (ASM_FieldNode)var4.next()).desc.equals("Lnet/minecraft/client/Minecraft;")) {
               this.mcFieldName = var5.name;
               var10000 = this.mcFieldName;
               break;
            }
         }
      }

      String var15 = var10000;
      boolean var16 = false;
      Iterator var17 = var3.methods.iterator();

      while (var17.hasNext()) {
         ASM_MethodNode var6;
         if (matchesMethod(var6 = (ASM_MethodNode)var17.next(), "updateCameraAndRender", "(FJ)V")) {
            var16 = true;
            ASM_InsnList var7;
            (var7 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var7.add(new ASM_FieldInsnNode(180, "net/minecraft/client/renderer/EntityRenderer", var15, "Lnet/minecraft/client/Minecraft;"));
            var7.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "preUpdateCameraAndRender", "(Lnet/minecraft/client/Minecraft;)V", false));
            var6.instructions.insert(var7);
            ASM_AbstractInsnNode[] var8;
            int var9 = (var8 = var6.instructions.toArray()).length;

            for (int var10 = 0; var10 < var9; var10++) {
               ASM_AbstractInsnNode var11;
               if ((var11 = var8[var10]).getOpcode() == 177) {
                  ASM_InsnList var12;
                  (var12 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var12.add(new ASM_FieldInsnNode(180, "net/minecraft/client/renderer/EntityRenderer", var15, "Lnet/minecraft/client/Minecraft;"));
                  var12.add(
                     new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "postUpdateCameraAndRender", "(Lnet/minecraft/client/Minecraft;)V", false)
                  );
                  var6.instructions.insertBefore(var11, var12);
               }
            }
         }

         if (matchesMethod(var6, "updateRenderer", "()V")) {
            var16 = true;
            ASM_InsnList var18;
            (var18 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var18.add(new ASM_FieldInsnNode(180, "net/minecraft/client/renderer/EntityRenderer", var15, "Lnet/minecraft/client/Minecraft;"));
            var18.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "preUpdateRenderer", "(Lnet/minecraft/client/Minecraft;)V", false));
            var6.instructions.insert(var18);
            ASM_AbstractInsnNode[] var29;
            int var38 = (var29 = var6.instructions.toArray()).length;

            for (int var49 = 0; var49 < var38; var49++) {
               ASM_AbstractInsnNode var59;
               if ((var59 = var29[var49]).getOpcode() == 177) {
                  ASM_InsnList var69;
                  (var69 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var69.add(new ASM_FieldInsnNode(180, "net/minecraft/client/renderer/EntityRenderer", var15, "Lnet/minecraft/client/Minecraft;"));
                  var69.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "postUpdateRenderer", "(Lnet/minecraft/client/Minecraft;)V", false));
                  var6.instructions.insertBefore(var59, var69);
               }
            }
         }

         if (matchesMethod(var6, "renderWorldPass", "(IFJ)V")) {
            ASM_AbstractInsnNode var19 = null;

            ASM_AbstractInsnNode[] var30;
            for (int var39 = (var30 = var6.instructions.toArray()).length - 1; var39 >= 0; var39--) {
               ASM_AbstractInsnNode var50;
               ASM_FieldInsnNode var60;
               if ((var50 = var30[var39]).getOpcode() == 180
                  && (var60 = (ASM_FieldInsnNode)var50).owner.equals(var3.name)
                  && var60.desc.equals("Z")
                  && (var60.name.equals("renderHand") || var60.name.equals("field_175074_C"))) {
                  var19 = var50;
                  break;
               }
            }

            for (int var40 = var30.length - 1; var40 >= 0 && var19 == null; var40--) {
               ASM_AbstractInsnNode var51;
               ASM_MethodInsnNode var61;
               if ((var51 = var30[var40]).getOpcode() == 182 && (var61 = (ASM_MethodInsnNode)var51).owner.equals(var3.name) && var61.desc.equals("(FI)V")) {
                  for (int var70 = var40 - 1; var70 >= 0 && var40 - var70 <= 40; var70--) {
                     ASM_AbstractInsnNode var13;
                     ASM_FieldInsnNode var14;
                     if ((var13 = var30[var70]).getOpcode() == 180 && (var14 = (ASM_FieldInsnNode)var13).owner.equals(var3.name) && var14.desc.equals("Z")) {
                        var19 = var13;
                        break;
                     }
                  }

                  if (var19 == null) {
                     var19 = var51;
                  }
                  break;
               }
            }

            if (var19 != null) {
               var16 = true;
               ASM_InsnList var41;
               (var41 = new ASM_InsnList()).add(new ASM_VarInsnNode(21, 1));
               var41.add(new ASM_VarInsnNode(23, 2));
               var41.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "onRenderWorldPass", "(IF)V", false));
               var6.instructions.insertBefore(var19, var41);
            }
         }

         if (matchesMethod(var6, "hurtCameraEffect", "(F)V")) {
            ASM_AbstractInsnNode[] var20;
            int var31 = (var20 = var6.instructions.toArray()).length;

            for (int var42 = 0; var42 < var31; var42++) {
               ASM_AbstractInsnNode var52;
               ASM_LdcInsnNode var62;
               if ((var52 = var20[var42]).getOpcode() == 18 && (var62 = (ASM_LdcInsnNode)var52).cst instanceof Float && (Float)var62.cst == 14.0F) {
                  ASM_InsnList var71;
                  (var71 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "onHurtCam", "(F)F", false));
                  var6.instructions.insert(var52, var71);
               }
            }
         }

         if (matchesMethod(var6, "getMouseOver", "(F)V")) {
            ASM_InsnList var21;
            (var21 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
            var21.add(new ASM_FieldInsnNode(180, "net/minecraft/client/renderer/EntityRenderer", var15, "Lnet/minecraft/client/Minecraft;"));
            var21.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "preGetMouseOver", "(Lnet/minecraft/client/Minecraft;)V", false));
            var6.instructions.insert(var21);
            ASM_AbstractInsnNode[] var32;
            int var43 = (var32 = var6.instructions.toArray()).length;

            for (int var53 = 0; var53 < var43; var53++) {
               ASM_AbstractInsnNode var63;
               if ((var63 = var32[var53]).getOpcode() == 177) {
                  ASM_InsnList var72;
                  (var72 = new ASM_InsnList()).add(new ASM_VarInsnNode(25, 0));
                  var72.add(new ASM_FieldInsnNode(180, "net/minecraft/client/renderer/EntityRenderer", var15, "Lnet/minecraft/client/Minecraft;"));
                  var72.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "postGetMouseOver", "(Lnet/minecraft/client/Minecraft;)V", false));
                  var6.instructions.insertBefore(var63, var72);
               }
            }

            int var33 = 0;
            var43 = 0;
            ASM_AbstractInsnNode[] var54;
            int var64 = (var54 = var6.instructions.toArray()).length;

            for (int var73 = 0; var73 < var64; var73++) {
               ASM_AbstractInsnNode var75;
               ASM_LdcInsnNode var76;
               if ((var75 = var54[var73]).getOpcode() == 18 && (var76 = (ASM_LdcInsnNode)var75).cst instanceof Double && (Double)var76.cst == 3.0) {
                  if (var43 == 1) {
                     (var21 = new ASM_InsnList()).add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "onPickRange", "(D)D", false));
                     var6.instructions.insert(var75, var21);
                  }

                  var43++;
               }

               if (var75.getOpcode() == 57 && var33 == 0) {
                  ASM_VarInsnNode var77 = (ASM_VarInsnNode)var75;
                  (var21 = new ASM_InsnList()).add(new ASM_VarInsnNode(24, var77.var));
                  var21.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "onRaytraceRange", "(D)D", false));
                  var21.add(new ASM_VarInsnNode(57, var77.var));
                  var6.instructions.insert(var75, var21);
                  var33++;
               }

               ASM_MethodInsnNode var78;
               if (var75.getOpcode() == 185 && (var78 = (ASM_MethodInsnNode)var75).name.equals("size") && var78.desc.equals("()I")) {
                  (var21 = new ASM_InsnList()).add(new ASM_InsnNode(89));
                  var21.add(new ASM_MethodInsnNode(184, "valor/hook/EntityRendererHooks", "onGetMouseOverEntities", "(Ljava/util/List;)V", false));
                  var6.instructions.insertBefore(var78, var21);
               }
            }
         }

         if (matchesMethod(var6, "orientCamera", "(F)V")) {
            ASM_AbstractInsnNode[] var25;
            int var34 = (var25 = var6.instructions.toArray()).length;

            for (int var45 = 0; var45 < var34; var45++) {
               ASM_AbstractInsnNode var55;
               ASM_MethodInsnNode var65;
               if ((var55 = var25[var45]).getOpcode() == 182
                  && isSameName((var65 = (ASM_MethodInsnNode)var55).name, "distanceTo")
                  && var65.desc.equals("(Lnet/minecraft/util/Vec3;)D")) {
                  var65.setStatic();
                  var65.owner = "valor/hook/EntityRendererHooks";
                  var65.name = "onOrientCamera";
                  var65.desc = "(Lnet/minecraft/util/Vec3;Lnet/minecraft/util/Vec3;F)D";
                  ASM_InsnList var74;
                  (var74 = new ASM_InsnList()).add(new ASM_VarInsnNode(23, 1));
                  var6.instructions.insertBefore(var65, var74);
               }
            }
         }

         if (matchesMethod(var6, "updateFogColor", "(F)V")) {
            ASM_AbstractInsnNode[] var26;
            int var35 = (var26 = var6.instructions.toArray()).length;

            for (int var46 = 0; var46 < var35; var46++) {
               ASM_AbstractInsnNode var56;
               ASM_MethodInsnNode var66;
               if ((var56 = var26[var46]).getOpcode() == 182
                  && isSameName((var66 = (ASM_MethodInsnNode)var56).name, "isPotionActive")
                  && var66.desc.equals("(Lnet/minecraft/potion/Potion;)Z")) {
                  var66.setStatic();
                  var66.owner = "valor/hook/EntityRendererHooks";
                  var66.name = "onBlindnessCheck";
                  var66.desc = "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/Potion;)Z";
               }
            }
         }

         if (matchesMethod(var6, "setupFog", "(IF)V")) {
            ASM_AbstractInsnNode[] var27;
            int var36 = (var27 = var6.instructions.toArray()).length;

            for (int var47 = 0; var47 < var36; var47++) {
               ASM_AbstractInsnNode var57;
               if ((var57 = var27[var47]).getOpcode() == 182) {
                  ASM_MethodInsnNode var67;
                  if (((var67 = (ASM_MethodInsnNode)var57).name.equals("getMaterial") || var67.name.equals("func_149688_o"))
                     && var67.desc.equals("()Lnet/minecraft/block/material/Material;")) {
                     var67.setStatic();
                     var67.owner = "valor/hook/EntityRendererHooks";
                     var67.name = "onSetupFogBlock";
                     var67.desc = "(Lnet/minecraft/block/Block;)Lnet/minecraft/block/material/Material;";
                  }

                  if (isSameName(var67.name, "isPotionActive") && var67.desc.equals("(Lnet/minecraft/potion/Potion;)Z")) {
                     var67.setStatic();
                     var67.owner = "valor/hook/EntityRendererHooks";
                     var67.name = "onBlindnessCheck";
                     var67.desc = "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/potion/Potion;)Z";
                  }
               }
            }
         }

         if (matchesMethod(var6, "setupCameraTransform", "(FI)V")) {
            ASM_AbstractInsnNode[] var28;
            int var37 = (var28 = var6.instructions.toArray()).length;

            for (int var48 = 0; var48 < var37; var48++) {
               ASM_AbstractInsnNode var58;
               ASM_MethodInsnNode var68;
               if ((var58 = var28[var48]).getOpcode() == 182
                  && isSameName((var68 = (ASM_MethodInsnNode)var58).name, "isPotionActive")
                  && var68.desc.equals("(Lnet/minecraft/potion/Potion;)Z")) {
                  var68.setStatic();
                  var68.owner = "valor/hook/EntityRendererHooks";
                  var68.name = "onNauseaCheck";
                  var68.desc = "(Lnet/minecraft/client/entity/EntityPlayerSP;Lnet/minecraft/potion/Potion;)Z";
               }
            }
         }
      }

      return !var16 ? var1 : writeClass(var3, var1);
   }

   public EntityRendererTransformer() {
      super("net.minecraft.client.renderer.EntityRenderer");
   }
}
