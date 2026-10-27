package valor.util.render;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;

public abstract class ShaderProgram {
   private final Map<String, Integer> uniformMap = new HashMap<>();
   protected int programId = GL20.glCreateProgram();

   private static int compileShader(String source, int shaderType) {
      int shader = GL20.glCreateShader(shaderType);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      if (GL20.glGetShaderi(shader, 35713) == 0) {
         System.err.println("[Valor] Shader compile failed: " + GL20.glGetShaderInfoLog(shader, 1024));
         return -1;
      } else {
         return shader;
      }
   }

   public abstract void initUniforms();

   public final int getUniformLocation(String uniformName) {
      return this.uniformMap.get(uniformName);
   }

   public final void registerUniform(String uniformName) {
      this.uniformMap.put(uniformName, GL20.glGetUniformLocation(this.programId, uniformName));
   }

   public abstract void useShader();

   public final boolean isValid() {
      return this.programId > 0;
   }

   public ShaderProgram(String fragmentSource) {
      int vertexShader = compileShader("#version 120\nvoid main(void) {\ngl_TexCoord[0] = gl_MultiTexCoord0;\ngl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;\n}", 35633);
      int fragShader = compileShader(fragmentSource, 35632);
      if (vertexShader != -1 && fragShader != -1) {
         GL20.glAttachShader(this.programId, vertexShader);
         GL20.glAttachShader(this.programId, fragShader);
         GL20.glLinkProgram(this.programId);
         if (GL20.glGetProgrami(this.programId, 35714) == 0) {
            System.err.println("[Valor] Shader link failed: " + GL20.glGetProgramInfoLog(this.programId, 1024));
            this.programId = -1;
         } else {
            this.initUniforms();
         }
      } else {
         System.err.println("[Valor] Shader program creation aborted due to compile failure");
         this.programId = -1;
      }
   }
}
