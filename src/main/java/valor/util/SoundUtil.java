package valor.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SoundUtil {
   public static final Logger LOGGER = LogManager.getLogger("Valor");
   public static final Minecraft mc = Minecraft.getMinecraft();

   public static void playToggleSound(int soundIndex, boolean enabled) {
      if (soundIndex <= 0) {
         playSound("random.click");
      } else {
         String soundPack;
         switch (soundIndex) {
            case 1:
               soundPack = "sigma";
               break;
            case 2:
               soundPack = "rise";
               break;
            case 3:
               soundPack = "quickmacro";
               break;
            default:
               playSound("random.click");
               return;
         }

         String soundName = "valor:toggle." + soundPack + "." + (enabled ? "enable" : "disable");

         try {
            if (mc.thePlayer != null) {
               mc.thePlayer.playSound(soundName, 1.0F, 1.0F);
            } else {
               playSound(soundName);
            }
         } catch (Throwable t) {
            LOGGER.warn("Failed to play {}: {}", new Object[]{soundName, t.toString()});
         }
      }
   }

   public static void playSound(String soundName) {
      try {
         SoundHandler soundHandler = mc.getSoundHandler();
         if (soundHandler != null) {
            soundHandler.playSound(PositionedSoundRecord.create(new ResourceLocation(soundName)));
         }
      } catch (Throwable t) {
         LOGGER.warn("playSound({}) failed: {}", new Object[]{soundName, t.toString()});
      }
   }
}
