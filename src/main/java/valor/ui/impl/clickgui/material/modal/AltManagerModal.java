package valor.ui.impl.clickgui.material.modal;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import valor.Valor;
import valor.auth.AccountStore;
import valor.auth.AltAccount;
import valor.auth.MicrosoftAuthService;
import valor.auth.OAuthLocalServer;
import valor.auth.SessionSwitcher;
import valor.module.render.AltManagerMod;
import valor.ui.impl.clickgui.material.MaterialTheme;
import valor.util.render.GreenRender;

public class AltManagerModal extends Modal {
   private static final int MODAL_W = 420;
   private static final int MODAL_H = 360;
   private static final int ROW_H = 26;
   private static final int BTN_H = 22;
   private static final int MAX_LIST_H = 230;
   private static final int BTN_COUNT = 6;
   private static final float BTN_W = 62.0F;
   private static final float BTN_GAP = 4.0F;
   private int scroll = 0;
   private String status = "Click an account to switch, or add one below.";
   private Color statusColor = MaterialTheme.TEXT_MUTED;
   private AltAccount pendingConfirm;
   private volatile boolean confirmExpired;
   private static final int INPUT_NONE = 0;
   private static final int INPUT_REFRESH = 1;
   private static final int INPUT_OFFLINE = 2;
   private static final int INPUT_ACCESS = 3;
   private int inputMode = 0;
   private String inputBuffer = "";
   private String inputPrompt = "";
   private String inputPlaceholder = "";
   private long lastCursorToggle;
   private boolean cursorVisible = true;

   private void setStatus(String message, Color color) {
      this.status = message;
      this.statusColor = color == null ? MaterialTheme.TEXT : color;
   }

   private void drawRow(AltAccount account, int index, float x, float y, float width, int mouseX, int mouseY) {
      AltAccount active = AccountStore.getActiveAccount();
      boolean isActive = active != null && active.username.equalsIgnoreCase(account.username);
      boolean isHovered = (float)mouseX >= x && (float)mouseX <= x + width && (float)mouseY >= y && (float)mouseY <= y + 26.0F;
      Color accent = MaterialTheme.hudAccent();
      Color bg = isActive ? MaterialTheme.accent(accent, 0.12F) : (isHovered ? MaterialTheme.MODAL_ROW_HOVER : MaterialTheme.MODAL_ROW);
      GreenRender.fillRR(x, y, width, 24.0F, 5.0F, bg);
      if (isActive || isHovered) {
         GreenRender.outlineRR(x, y, width, 24.0F, 5.0F, 1.0F, isActive ? MaterialTheme.accent(accent, 0.34F) : MaterialTheme.GLASS_EDGE_SOFT);
      }

      Color badgeColor = account.accountType == AltAccount.AccountType.MICROSOFT ? MaterialTheme.MODAL_BADGE_MSA : MaterialTheme.MODAL_BADGE_OFFLINE;
      GreenRender.fillRR(x + 9.0F, y + 6.0F, 36.0F, 12.0F, 4.0F, MaterialTheme.accent(badgeColor, 0.28F));
      String badgeText = account.accountType == AltAccount.AccountType.MICROSOFT ? "MSA" : "OFF";
      GreenRender.drawStringSmall(
         badgeText,
         x + 9.0F + (36.0F - GreenRender.strWSmall(badgeText)) / 2.0F,
         y + (24.0F - GreenRender.fontHSmall()) / 2.0F + 0.5F,
         Color.WHITE
      );
      GreenRender.drawStringSmall(account.username, x + 53.0F, y + (24.0F - GreenRender.fontHSmall()) / 2.0F + 0.5F, MaterialTheme.TEXT);
      if (isActive) {
         GreenRender.fillRR(x + width - 13.0F, y + 10.0F, 4.0F, 4.0F, 2.0F, MaterialTheme.MODAL_OK);
      }

      if (isHovered && !isActive) {
         float actionX = x + width - 60.0F;
         GreenRender.drawStringSmall("Login", actionX, y + 6.0F, MaterialTheme.MODAL_OK);
         GreenRender.drawStringSmall("Del", actionX + 36.0F, y + 6.0F, MaterialTheme.MODAL_DANGER);
      }
   }

   private void confirmInput() {
      String trimmed = this.inputBuffer.trim();
      int currentMode = this.inputMode;
      this.inputMode = 0;
      this.inputBuffer = "";
      if (trimmed.isEmpty()) {
         this.setStatus("Empty input.", MaterialTheme.MODAL_DANGER);
      } else if (currentMode == INPUT_REFRESH) {
         this.addFromRefresh(trimmed);
      } else if (currentMode == INPUT_ACCESS) {
         this.addFromAccessToken(trimmed);
      } else if (currentMode == INPUT_OFFLINE) {
         AccountStore.addOrUpdateAccount(AltAccount.createOffline(trimmed));
         this.setStatus("Added offline account " + trimmed, MaterialTheme.MODAL_OK);
      }
   }

   private void doSwitch(AltAccount account) {
      if (account.accountType != AltAccount.AccountType.MICROSOFT
         || account.accessToken != null && (account.expiresAt <= 0L || System.currentTimeMillis() < account.expiresAt)) {
         SessionSwitcher.SwitchResult result = SessionSwitcher.switchSession(account);
         this.setStatus(result.message, result.success ? MaterialTheme.MODAL_OK : MaterialTheme.MODAL_DANGER);
      } else {
         this.refreshAndSwitch(account);
      }
   }

   private void drawActionBar(float x, float y, float width, float height) {
      GreenRender.fillRR(x, y, width, height, 6.0F, MaterialTheme.SEARCH_BG);
      GreenRender.outlineRR(x, y, width, height, 6.0F, 1.0F, MaterialTheme.GLASS_EDGE);
   }

   private void drawInputOverlay(float x, float y, float width, float height, int mouseX, int mouseY) {
      GreenRender.fillRR(x, y, width, height, 0.0F, new Color(0, 0, 0, 120));
      float fieldW = width - 48.0F;
      float fieldX = x + 24.0F;
      float fieldY = y + height / 2.0F - 30.0F;
      GreenRender.drawStringSmall(this.inputPrompt, fieldX, fieldY - 14.0F, MaterialTheme.TEXT);
      GreenRender.fillRR(fieldX, fieldY, fieldW, 26.0F, 6.0F, MaterialTheme.SEARCH_BG);
      GreenRender.outlineRR(fieldX, fieldY, fieldW, 26.0F, 6.0F, 1.2F, MaterialTheme.hudAccent());

      long now = System.currentTimeMillis();
      if (now - this.lastCursorToggle > 500L) {
         this.cursorVisible = !this.cursorVisible;
         this.lastCursorToggle = now;
      }

      String displayText = this.inputBuffer.isEmpty() ? this.inputPlaceholder : truncateForField(this.inputBuffer, fieldW - 24.0F);
      Color textColor = this.inputBuffer.isEmpty() ? MaterialTheme.TEXT_MUTED : MaterialTheme.TEXT;
      if (this.cursorVisible && (this.inputBuffer.isEmpty() || this.inputBuffer.length() < 48)) {
         displayText = displayText + "|";
      }

      GreenRender.drawStringSmall(displayText, fieldX + 10.0F, fieldY + (26.0F - GreenRender.fontHSmall()) / 2.0F + 0.5F, textColor);
      float btnY = y + height - 22.0F - 8.0F;
      float btnW = (width - 24.0F - 8.0F) / 2.0F;
      this.drawActionBar(x + 12.0F, btnY, width - 24.0F, 22.0F);
      this.drawButton(x + 12.0F, btnY, btnW, 22.0F, "Confirm", mouseX, mouseY, 10);
      this.drawButton(x + 12.0F + btnW + 8.0F, btnY, btnW, 22.0F, "Cancel", mouseX, mouseY, 11);
   }

   private void addFromAccessToken(String token) {
      if (MicrosoftAuthService.cleanToken(token).isEmpty()) {
         this.setStatus("Empty access token.", MaterialTheme.MODAL_DANGER);
      } else {
         this.setStatus("Validating access token...", MaterialTheme.TEXT_MUTED);
         new Thread(() -> {
            try {
               MicrosoftAuthService.MinecraftProfile profile = MicrosoftAuthService.loginWithAccessToken(token);
               AccountStore.addOrUpdateAccount(AltAccount.createMicrosoft(profile.username, profile.uuid, profile.accessToken, null, profile.expiresAt));
               this.setStatus("Added " + profile.username, MaterialTheme.MODAL_OK);
            } catch (Exception e) {
               this.setStatus("Token login failed: " + e.getMessage(), MaterialTheme.MODAL_DANGER);
            }
         }, "valor-alt-addtoken").start();
      }
   }

   private void addFromCookies(String cookieData) {
      this.setStatus("Authorizing with cookies...", MaterialTheme.TEXT_MUTED);
      new Thread(
            () -> {
               try {
                  MicrosoftAuthService.OAuthTokens tokens = MicrosoftAuthService.loginWithCookies(cookieData);
                  MicrosoftAuthService.MinecraftProfile profile = MicrosoftAuthService.loginWithXblToken(tokens.accessToken);
                  AccountStore.addOrUpdateAccount(
                     AltAccount.createMicrosoft(
                        profile.username,
                        profile.uuid,
                        profile.accessToken,
                        tokens.refreshToken,
                        profile.expiresAt
                     )
                  );
                  this.setStatus("Logged in as " + profile.username, MaterialTheme.MODAL_OK);
               } catch (Exception e) {
                  this.setStatus("Cookie login failed: " + e.getMessage(), MaterialTheme.MODAL_DANGER);
               }
            },
            "valor-alt-cookie"
         )
         .start();
   }

   private void addFromRefresh(String token) {
      if (MicrosoftAuthService.cleanRefreshToken(token).isEmpty()) {
         this.setStatus("Empty refresh token.", MaterialTheme.MODAL_DANGER);
      } else {
         this.setStatus("Exchanging refresh token...", MaterialTheme.TEXT_MUTED);
         new Thread(
               () -> {
                  try {
                     MicrosoftAuthService.RefreshResult result = MicrosoftAuthService.loginWithRefreshToken(token);
                     AccountStore.addOrUpdateAccount(
                        AltAccount.createMicrosoft(
                           result.profile.username,
                           result.profile.uuid,
                           result.profile.accessToken,
                           result.tokens.refreshToken,
                           result.profile.expiresAt
                        )
                     );
                     this.setStatus("Added " + result.profile.username, MaterialTheme.MODAL_OK);
                  } catch (Exception e) {
                     this.setStatus("Add failed: " + e.getMessage(), MaterialTheme.MODAL_DANGER);
                  }
               },
               "valor-alt-addrefresh"
            )
            .start();
      }
   }

   private void attemptLogin(AltAccount account) {
      AltManagerMod mod = (AltManagerMod)Valor.moduleManager.modules.get(AltManagerMod.class);
      if ((mod == null || (Boolean)mod.confirmSwitchProp.getValue()) && this.pendingConfirm != account) {
         this.pendingConfirm = account;
         this.setStatus("Click again to confirm login as " + account.username, MaterialTheme.MODAL_OK);
         new Thread(() -> {
            try {
               Thread.sleep(4000L);
            } catch (InterruptedException ignored) {
            }

            if (this.pendingConfirm == account) {
               this.pendingConfirm = null;
               this.confirmExpired = true;
            }
         }, "valor-alt-confirm-timeout").start();
      } else {
         this.pendingConfirm = null;
         this.doSwitch(account);
      }
   }

   private void cancelInput() {
      this.inputMode = 0;
      this.inputBuffer = "";
      this.setStatus("Cancelled.", MaterialTheme.TEXT_MUTED);
   }

   static void access$000(AltManagerModal modal, String message, java.awt.Color color) {
      modal.setStatus(message, color);
   }

   private void promptRefreshToken() {
      this.startInput(INPUT_REFRESH, "Paste refresh token", "M.C...");
   }

   private void promptAccessToken() {
      this.startInput(INPUT_ACCESS, "Paste Minecraft access token", "eyJ...");
   }

   private void promptOffline() {
      this.startInput(INPUT_OFFLINE, "Offline username", "Steve");
   }

   private void startBrowserLogin() {
      this.setStatus("Starting local auth server...", MaterialTheme.TEXT_MUTED);
      new Thread(() -> {
         try {
            OAuthLocalServer server = new OAuthLocalServer(new AltManagerModal$1(this));
            String authUrl = server.start();
            this.setStatus("Browser opened \u2014 log in with email:pass", MaterialTheme.TEXT);
            MicrosoftAuthService.openUrlInBrowser(authUrl);
            new Thread(() -> {
               try {
                  Thread.sleep(300000L);
               } catch (InterruptedException ignored) {
               }
               server.stop();
            }, "valor-alt-browserserver-timeout").start();
         } catch (Throwable t) {
            this.setStatus("Couldn't start auth server: " + t.getMessage(), MaterialTheme.MODAL_DANGER);
         }
      }, "valor-alt-browserstart").start();
   }

   private static String truncateForField(String text, float maxWidth) {
      if (text != null && !text.isEmpty()) {
         if (GreenRender.strWSmall(text) <= maxWidth) {
            return text;
         } else {
            String ellipsis = "...";
            for (int len = Math.min(text.length(), 80); len >= 8; len--) {
               String candidate = ellipsis + text.substring(text.length() - len);
               if (GreenRender.strWSmall(candidate) <= maxWidth) {
                  return candidate;
               }
            }
            return ellipsis;
         }
      } else {
         return text;
      }
   }

   private void startInput(int mode, String prompt, String placeholder) {
      this.inputMode = mode;
      this.inputBuffer = "";
      this.inputPrompt = prompt;
      this.inputPlaceholder = placeholder;
      this.lastCursorToggle = System.currentTimeMillis();
      this.cursorVisible = true;
   }

   private int maxInputLen() {
      return this.inputMode != INPUT_REFRESH && this.inputMode != INPUT_ACCESS ? 128 : 4096;
   }

   private void refreshAndSwitch(AltAccount account) {
      if (account.refreshToken == null) {
         this.setStatus("No refresh token for " + account.username + " \u2014 re-add the account.", MaterialTheme.MODAL_DANGER);
      } else {
         this.setStatus("Refreshing " + account.username + "...", MaterialTheme.TEXT_MUTED);
         new Thread(() -> {
            try {
               MicrosoftAuthService.RefreshResult result = MicrosoftAuthService.loginWithRefreshToken(account.refreshToken);
               account.accessToken = result.profile.accessToken;
               account.uuid = result.profile.uuid;
               account.username = result.profile.username;
               account.refreshToken = result.tokens.refreshToken != null ? result.tokens.refreshToken : account.refreshToken;
               account.expiresAt = result.profile.expiresAt;
               AccountStore.save();
               SessionSwitcher.SwitchResult switchResult = SessionSwitcher.switchSession(account);
               this.setStatus(switchResult.message, switchResult.success ? MaterialTheme.MODAL_OK : MaterialTheme.MODAL_DANGER);
            } catch (Exception e) {
               this.setStatus("Refresh failed: " + e.getMessage(), MaterialTheme.MODAL_DANGER);
            }
         }, "valor-alt-switch").start();
      }
   }

   private void pasteFromClipboard() {
      String clipboardText = GuiScreen.getClipboardString();
      if (clipboardText != null && !clipboardText.isEmpty()) {
         clipboardText = clipboardText.replace('\r', ' ').replace('\n', ' ').trim();
         int remainingSpace = this.maxInputLen() - this.inputBuffer.length();
         if (remainingSpace > 0) {
            if (clipboardText.length() > remainingSpace) {
               clipboardText = clipboardText.substring(0, remainingSpace);
            }
            this.inputBuffer = this.inputBuffer + clipboardText;
         }
      }
   }

   private void promptCookieLogin() {
      this.setStatus("Opening file picker...", MaterialTheme.TEXT_MUTED);
      new Thread(() -> {
         FileDialog fileDialog = null;
         try {
            Frame dummyFrame = new Frame();
            dummyFrame.setUndecorated(true);
            dummyFrame.setSize(0, 0);
            fileDialog = new FileDialog(dummyFrame, "Select cookies.txt", FileDialog.LOAD);
            fileDialog.setMultipleMode(false);
            fileDialog.setVisible(true);
            String selectedFile = fileDialog.getFile();
            String selectedDir = fileDialog.getDirectory();
            dummyFrame.dispose();
            if (selectedFile != null && selectedDir != null) {
               File file = new File(selectedDir, selectedFile);
               if (!file.exists()) {
                  this.setStatus("File not found: " + file.getName(), MaterialTheme.MODAL_DANGER);
                  return;
               }

               String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
               this.setStatus("Loaded " + file.getName() + " — authorizing...", MaterialTheme.TEXT_MUTED);
               this.addFromCookies(content);
               return;
            }

            this.setStatus("No file selected.", MaterialTheme.TEXT_MUTED);
         } catch (Throwable t) {
            this.setStatus("File picker failed: " + t.getClass().getSimpleName() + ": " + t.getMessage(), MaterialTheme.MODAL_DANGER);
         } finally {
            if (fileDialog != null) {
               try {
                  fileDialog.dispose();
               } catch (Throwable ignored) {
               }
            }
         }
      }, "valor-alt-filepicker").start();
   }

   @Override
   protected boolean clickBody(int mouseX, int mouseY, int mouseButton) {
      if (mouseButton != 0) {
         return true;
      } else {
         float bx = this.bodyX();
         float by = this.bodyY();
         float bw = this.bodyW();
         float bh = this.bodyH();
         float barY = by + bh - 22.0F - 8.0F;

         if (this.inputMode != INPUT_NONE) {
            float btnW = (bw - 24.0F - 8.0F) / 2.0F;
            if ((float)mouseY >= barY && (float)mouseY <= barY + 22.0F) {
               if ((float)mouseX >= bx + 12.0F && (float)mouseX <= bx + 12.0F + btnW) {
                  this.confirmInput();
                  return true;
               }
               if ((float)mouseX >= bx + 12.0F + btnW + 8.0F && (float)mouseX <= bx + 12.0F + btnW * 2.0F + 8.0F) {
                  this.cancelInput();
                  return true;
               }
            }
            return true;
         } else {
            if ((float)mouseY >= barY && (float)mouseY <= barY + 22.0F) {
               float startX = bx + 8.0F;
               for (int i = 0; i < BTN_COUNT; i++) {
                  float btnX1 = startX + (float)i * (BTN_W + BTN_GAP);
                  float btnX2 = btnX1 + BTN_W;
                  if ((float)mouseX >= btnX1 && (float)mouseX <= btnX2) {
                     switch (i) {
                        case 0:
                           this.promptCookieLogin();
                           return true;
                        case 1:
                           this.startBrowserLogin();
                           return true;
                        case 2:
                           this.promptRefreshToken();
                           return true;
                        case 3:
                           this.promptAccessToken();
                           return true;
                        case 4:
                           this.promptOffline();
                           return true;
                        case 5:
                           this.close();
                           return true;
                     }
                  }
               }
            }

            List<AltAccount> accounts = AccountStore.getAccounts();
            int listH = Math.min(accounts.size() * ROW_H, MAX_LIST_H);
            float listY = by + 8.0F;
            if ((float)mouseY < listY || (float)mouseY > listY + (float)listH) {
               return true;
            } else {
               int clickedIdx = (int)(((float)mouseY - listY + (float)this.scroll) / (float)ROW_H);
               if (clickedIdx >= 0 && clickedIdx < accounts.size()) {
                  AltAccount account = accounts.get(clickedIdx);
                  float rowX = bx + 8.0F;
                  float rowW = this.bodyW() - 16.0F;
                  float actionX = rowX + rowW - 60.0F;
                  if ((float)mouseX >= actionX - 4.0F && (float)mouseX <= actionX + 30.0F) {
                     this.attemptLogin(account);
                     return true;
                  } else if ((float)mouseX >= actionX + 32.0F && (float)mouseX <= actionX + 60.0F) {
                     AccountStore.removeAccount(clickedIdx);
                     this.setStatus("Removed " + account.username, MaterialTheme.MODAL_DANGER);
                     return true;
                  } else if ((float)mouseX >= rowX && (float)mouseX <= rowX + rowW - 64.0F) {
                     this.attemptLogin(account);
                     return true;
                  }
               }
               return true;
            }
         }
      }
   }

   @Override
   protected void renderBody(int mouseX, int mouseY, float partialTicks) {
      if (this.confirmExpired) {
         this.confirmExpired = false;
         if (this.pendingConfirm == null) {
            this.status = "Click an account to switch, or add one below.";
            this.statusColor = MaterialTheme.TEXT_MUTED;
         }
      }

      float bx = this.bodyX();
      float by = this.bodyY();
      float bw = this.bodyW();
      float bh = this.bodyH();
      List<AltAccount> accounts = AccountStore.getAccounts();
      int totalListH = accounts.size() * ROW_H;
      int visibleListH = Math.min(totalListH, MAX_LIST_H);
      int maxScroll = Math.max(0, totalListH - visibleListH);

      if (this.scroll > maxScroll) {
         this.scroll = maxScroll;
      }
      if (this.scroll < 0) {
         this.scroll = 0;
      }

      float listY = by + 8.0F;
      GreenRender.pushScissor(bx, listY, bw, (float)visibleListH);

      try {
         int rowY = (int)listY - this.scroll;
         for (int i = 0; i < accounts.size(); i++) {
            AltAccount account = accounts.get(i);
            this.drawRow(account, i, bx + 8.0F, (float)rowY, bw - 16.0F, mouseX, mouseY);
            rowY += ROW_H;
         }
      } finally {
         GreenRender.popScissor();
      }

      if (accounts.isEmpty()) {
         GreenRender.drawStringSmall("No accounts yet. Use the buttons below.", bx + 14.0F, by + 30.0F, MaterialTheme.TEXT_MUTED);
      }

      if (this.inputMode != INPUT_NONE) {
         this.drawInputOverlay(bx, by, bw, bh, mouseX, mouseY);
      } else {
         float statusY = by + bh - 22.0F - 36.0F;
         GreenRender.drawStringSmall(this.status, bx + 12.0F, statusY, this.statusColor);
         float barY = by + bh - 22.0F - 8.0F;
         float barX = bx + 8.0F;
         this.drawActionBar(barX, barY, 392.0F, 22.0F);
         this.drawButton(barX + 0.0F, barY, 62.0F, 22.0F, "+ Cookie", mouseX, mouseY, 0);
         this.drawButton(barX + 66.0F, barY, 62.0F, 22.0F, "+ Browser", mouseX, mouseY, 1);
         this.drawButton(barX + 132.0F, barY, 62.0F, 22.0F, "+ Refresh", mouseX, mouseY, 2);
         this.drawButton(barX + 198.0F, barY, 62.0F, 22.0F, "+ Token", mouseX, mouseY, 3);
         this.drawButton(barX + 264.0F, barY, 62.0F, 22.0F, "+ Offline", mouseX, mouseY, 4);
         this.drawButton(barX + 330.0F, barY, 62.0F, 22.0F, "Close", mouseX, mouseY, 5);
      }
   }

   @Override
   public void onScroll(int mouseX, int mouseY, int scrollAmount) {
      int totalH = AccountStore.getAccounts().size() * ROW_H;
      int visibleH = Math.min(totalH, MAX_LIST_H);
      if (totalH > visibleH) {
         this.scroll -= scrollAmount * 18;
         int maxScroll = Math.max(0, totalH - visibleH);
         this.scroll = Math.max(0, Math.min(this.scroll, maxScroll));
      }
   }

   private void drawButton(float x, float y, float width, float height, String label, int mouseX, int mouseY, int buttonId) {
      if ((float)mouseX >= x && (float)mouseX <= x + width && (float)mouseY >= y && (float)mouseY <= y + height) {
         GreenRender.fillRR(x, y, width, height, 5.0F, MaterialTheme.accent(MaterialTheme.hudAccent(), 0.12F));
      }

      Color labelColor = buttonId == 5 ? MaterialTheme.TEXT_MUTED : MaterialTheme.TEXT;
      GreenRender.drawStringSmall(label, x + (width - GreenRender.strWSmall(label)) / 2.0F, y + (height - GreenRender.fontHSmall()) / 2.0F + 0.5F, labelColor);
   }

   @Override
   public void keyTyped(char typedChar, int keyCode) {
      if (this.inputMode != INPUT_NONE) {
         if (keyCode == 1) {
            this.cancelInput();
         } else if (keyCode == 28 || keyCode == 156) {
            this.confirmInput();
         } else if (keyCode == 14) {
            if (!this.inputBuffer.isEmpty()) {
               this.inputBuffer = this.inputBuffer.substring(0, this.inputBuffer.length() - 1);
            }
         } else if (keyCode == 47 && GuiScreen.isCtrlKeyDown()) {
            this.pasteFromClipboard();
         } else {
            if (typedChar >= ' ' && typedChar != 127 && this.inputBuffer.length() < this.maxInputLen()) {
               this.inputBuffer = this.inputBuffer + typedChar;
            }
         }
      }
   }

   public AltManagerModal() {
      super("Alt Manager", 420, 360);
   }
}
