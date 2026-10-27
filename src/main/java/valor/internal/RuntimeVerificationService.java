package valor.internal;

public final class RuntimeVerificationService {

   public static VerificationResult verifyRuntime() {
      return new VerificationResult(true, null);
   }

   public static class VerificationResult {
      public final boolean success;
      private final String message;

      public VerificationResult(boolean success, String message) {
         this.success = success;
         this.message = message;
      }

      public static VerificationResult failure(String reason) {
         return new VerificationResult(false, reason);
      }

      public boolean isSuccess() {
         return this.success;
      }

      public String getMessage() {
         return this.message;
      }
   }
}
