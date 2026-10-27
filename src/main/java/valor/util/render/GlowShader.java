package valor.util.render;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

public final class GlowShader extends ShaderProgram {
   private static final String FRAGMENT_SHADER = String.join(
      "\n",
      "#version 120",
      "uniform sampler2D texture;",
      "uniform vec2 texel;",
      "uniform vec4 glowColor;",
      "void main() {",
      "  vec2 uv = gl_TexCoord[0].xy;",
      "  float center = texture2D(texture, uv).a;",
      "  vec2 nearStep = texel * 2.0;",
      "  vec2 farStep = texel * 5.0;",
      "  float nearMask = 0.0;",
      "  float farMask = 0.0;",
      "  for (int x = -1; x <= 1; x++) {",
      "    for (int y = -1; y <= 1; y++) {",
      "      if (x == 0 && y == 0) continue;",
      "      vec2 direction = vec2(float(x), float(y));",
      "      nearMask += texture2D(texture, uv + direction * nearStep).a;",
      "      farMask += texture2D(texture, uv + direction * farStep).a;",
      "    }",
      "  }",
      "  float halo = clamp((nearMask * 0.13 + farMask * 0.055) * (1.0 - center), 0.0, 1.0);",
      "  gl_FragColor = vec4(glowColor.rgb, halo * glowColor.a);",
      "}"
   );
   public Color glowColor = Color.WHITE;

   @Override
   public final void initUniforms() {
      this.registerUniform("texture");
      this.registerUniform("texel");
      this.registerUniform("glowColor");
   }

   @Override
   public final void useShader() {
      GL20.glUseProgram(super.programId);
      GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      Minecraft mc = Minecraft.getMinecraft();
      GL20.glUniform2f(this.getUniformLocation("texel"), 1.0F / (float)Math.max(1, mc.displayWidth), 1.0F / (float)Math.max(1, mc.displayHeight));
      GL20.glUniform4f(
         this.getUniformLocation("glowColor"),
         (float)this.glowColor.getRed() / 255.0F,
         (float)this.glowColor.getGreen() / 255.0F,
         (float)this.glowColor.getBlue() / 255.0F,
         (float)this.glowColor.getAlpha() / 255.0F
      );
   }

   public GlowShader() {
      super(FRAGMENT_SHADER);
   }
}
