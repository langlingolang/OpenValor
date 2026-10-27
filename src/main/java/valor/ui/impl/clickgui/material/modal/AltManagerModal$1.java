package valor.ui.impl.clickgui.material.modal;

import valor.auth.AccountStore;
import valor.auth.AltAccount;
import valor.auth.MicrosoftAuthService;
import valor.auth.OAuthLocalServer;
import valor.ui.impl.clickgui.material.MaterialTheme;

class AltManagerModal$1 implements OAuthLocalServer.OAuthCallback {
   private final AltManagerModal modal;

   @Override
   public void onSuccess(MicrosoftAuthService.MinecraftProfile profile, String refreshToken) {
      AccountStore.addOrUpdateAccount(AltAccount.createMicrosoft(profile.username, profile.uuid, profile.accessToken, refreshToken, profile.expiresAt));
      AltManagerModal.access$000(this.modal, "Logged in as " + profile.username, MaterialTheme.MODAL_OK);
   }

   @Override
   public void onCancelled() {
      AltManagerModal.access$000(this.modal, "Browser auth cancelled.", MaterialTheme.TEXT_MUTED);
   }

   @Override
   public void onStatus(String message, boolean isMuted) {
      AltManagerModal.access$000(this.modal, message, isMuted ? MaterialTheme.TEXT_MUTED : MaterialTheme.MODAL_DANGER);
   }

   AltManagerModal$1(AltManagerModal modal) {
      this.modal = modal;
   }
}
