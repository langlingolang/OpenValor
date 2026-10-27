package valor.util.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

public final class OutlineShader extends ShaderProgram {
   public float red = 1.0F;
   public float green = 1.0F;
   public float blue = 1.0F;
   public float alpha = 1.0F;
   public float radius = 2.0F;

   private static final String FRAGMENT_SHADER = String.join(
      "\n",
      "#version 120",
      "uniform sampler2D texture;",
      "uniform vec2 size;",
      "uniform float radius;",
      "uniform vec4 outlineColor;",
      "void main(void) {",
      "vec4 xy = texture2D(texture, gl_TexCoord[0].xy);",
      "gl_FragColor = vec4(0.0);",
      "if(xy.a != 0) {",
      "gl_FragColor = vec4(0, 0, 0, 0);",
      "} else {",
      "for (float x = -radius; x <= radius; x++) {",
      "for (float y = -radius; y <= radius; y++) {",
      "vec4 color = texture2D(texture, gl_TexCoord[0].xy + vec2(size.x * x, size.y * y));",
      "if (color.a != 0) {",
      "gl_FragColor = outlineColor;",
      "}",
      "}",
      "}",
      "}",
      "}"
   );

   @Override
   public final void initUniforms() {
      this.registerUniform("texture");
      this.registerUniform("size");
      this.registerUniform("radius");
      this.registerUniform("outlineColor");
   }

   @Override
   public final void useShader() {
      GL20.glUseProgram(super.programId);
      GL20.glUniform1i(this.getUniformLocation("texture"), 0);
      int sizeUniform = this.getUniformLocation("size");
      float texelWidth = 1.0F / (float)Minecraft.getMinecraft().displayWidth;
      float texelHeight = 1.0F / (float)Minecraft.getMinecraft().displayHeight;
      GL20.glUniform2f(sizeUniform, texelWidth, texelHeight);
      GL20.glUniform1f(this.getUniformLocation("radius"), this.radius);
      GL20.glUniform4f(this.getUniformLocation("outlineColor"), this.red, this.green, this.blue, this.alpha);
   }

   public OutlineShader() {
      super(FRAGMENT_SHADER);
   }
}
