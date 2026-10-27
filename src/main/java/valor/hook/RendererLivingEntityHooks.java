package valor.hook;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import valor.Valor;
import valor.event.EventManager;
import valor.event.types.EventType;
import valor.events.RenderLivingEvent;
import valor.module.render.ESP;
import valor.module.render.NameTags;

public class RendererLivingEntityHooks {
   private static volatile boolean isRenderingName = false;
   private static volatile boolean shouldSuppressName = false;

   public static boolean isSuppressingName() {
      return isRenderingName && shouldSuppressName;
   }

   public static void onRenderNameEnd() {
      isRenderingName = false;
      shouldSuppressName = false;
   }

   public static void onRenderNameStart(EntityLivingBase entity) {
      isRenderingName = true;
      shouldSuppressName = false;
      if (Valor.moduleManager != null) {
         NameTags nameTags;
         if ((nameTags = (NameTags)Valor.moduleManager.modules.get(NameTags.class)) != null && nameTags.isEnabled() && nameTags.isValidEntity(entity)) {
            shouldSuppressName = true;
         }

         ESP esp;
         if ((esp = (ESP)Valor.moduleManager.modules.get(ESP.class)) != null && esp.isEnabled() && !esp.isOutlineMode()) {
            shouldSuppressName = true;
         }
      }
   }

   public static void onDoRenderPost(EntityLivingBase entity) {
      EventManager.call(new RenderLivingEvent(EventType.POST, entity));
   }

   public static void onDoRenderPre(EntityLivingBase entity) {
      EventManager.call(new RenderLivingEvent(EventType.PRE, entity));
   }

   public static boolean shouldHideVanillaNameTag(Entity entity) {
      if (ESP.isRenderingOutline()) {
         return true;
      } else if (entity instanceof EntityLivingBase && Valor.moduleManager != null && Minecraft.getMinecraft().getRenderViewEntity() != null) {
         NameTags nameTags;
         return (nameTags = (NameTags)Valor.moduleManager.getModule(NameTags.class)) != null && nameTags.isEnabled() ? nameTags.isValidEntity((EntityLivingBase)entity) : false;
      } else {
         return false;
      }
   }
}
