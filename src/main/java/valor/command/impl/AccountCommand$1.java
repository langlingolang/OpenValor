package valor.command.impl;

import valor.Valor;
import valor.auth.AccountStore;
import valor.auth.AltAccount;
import valor.auth.MicrosoftAuthService;
import valor.auth.OAuthLocalServer;
import valor.util.ChatUtil;

final class AccountCommand$1 implements OAuthLocalServer.OAuthCallback {
   private final AccountCommand parentCommand;

   @Override
   public final void onSuccess(MicrosoftAuthService.MinecraftProfile profile, String refreshToken) {
      AccountStore.addOrUpdateAccount(AltAccount.createMicrosoft(profile.username, profile.uuid, profile.accessToken, refreshToken, profile.expiresAt));
      ChatUtil.addFormattedChatMessage(String.format("%sAdded Microsoft account &o%s&r&r", Valor.CHAT_PREFIX, profile.username));
   }

   @Override
   public final void onCancelled() {
      ChatUtil.addFormattedChatMessage(String.format("%sBrowser auth cancelled.&r", Valor.CHAT_PREFIX));
   }

   @Override
   public final void onStatus(String message, boolean isError) {
      ChatUtil.addFormattedChatMessage(String.format("%s%s&r", Valor.CHAT_PREFIX, message));
   }

   AccountCommand$1(AccountCommand parent) {
      super();
      this.parentCommand = parent;
   }
}
