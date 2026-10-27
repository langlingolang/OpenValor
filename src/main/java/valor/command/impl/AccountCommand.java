package valor.command.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import valor.Valor;
import valor.auth.AccountStore;
import valor.auth.AltAccount;
import valor.auth.MicrosoftAuthService;
import valor.auth.OAuthLocalServer;
import valor.auth.SessionSwitcher;
import valor.command.Command;
import valor.util.ChatUtil;

public final class AccountCommand extends Command {
   private static void sendUsage(String commandName) {
      ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s <&ol(ist)&r/&ologin&r/&oadd&r/&oremove&r/&orefresh&r/&oactive&r>&r", Valor.CHAT_PREFIX, commandName.toLowerCase(Locale.ROOT)));
   }

   private static int parseIndex(String str) {
      try {
         return Integer.parseInt(str.trim());
      } catch (Exception e) {
         return -1;
      }
   }

   @Override
   public void execute(ArrayList<String> args) {
      if (args.size() < 2) {
         sendUsage(args.get(0));
      } else {
         String subCommand = args.get(1).toLowerCase(Locale.ROOT);
         switch (subCommand) {
            case "l":
            case "list":
               List<AltAccount> accounts;
               if ((accounts = AccountStore.getAccounts()).isEmpty()) {
                  ChatUtil.addFormattedChatMessage(String.format("%sNo saved accounts. Use .alt add offline <name>&r", Valor.CHAT_PREFIX));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sAccounts (&o%d&r):&r", Valor.CHAT_PREFIX, accounts.size()));
               AltAccount activeAccount = AccountStore.getActiveAccount();

               for (int i = 0; i < accounts.size(); i++) {
                  AltAccount account = accounts.get(i);
                  String activeMarker = activeAccount != null && activeAccount.username.equalsIgnoreCase(account.username) ? " &a*&r" : "";
                  ChatUtil.addRawChatMessage(
                     String.format(
                        "   &7%d&r &f%s&r &7(%s)&r%s", i, account.username, account.accountType.name().toLowerCase(Locale.ROOT), activeMarker.replace("&", "\u00a7")
                     )
                  );
               }
               return;

            case "login":
            case "use":
               if (args.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s login <&oindex&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                  return;
               } else {
                  AltAccount targetAccount;
                  if ((targetAccount = AccountStore.getAccount(parseIndex(args.get(2)))) == null) {
                     ChatUtil.addFormattedChatMessage(String.format("%sNo account at index &o%s&r&r", Valor.CHAT_PREFIX, args.get(2)));
                     return;
                  }

                  SessionSwitcher.SwitchResult switchResult = SessionSwitcher.switchSession(targetAccount);
                  ChatUtil.addFormattedChatMessage(String.format("%s%s&r", Valor.CHAT_PREFIX, switchResult.message));
                  return;
               }

            case "add":
               if (args.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add <&ooffline&r/&ocookie&r/&obrowser&r/&orefresh&r/&otoken&r> ...&r", Valor.CHAT_PREFIX, args.get(0)));
                  return;
               } else {
                  String addType = args.get(2).toLowerCase(Locale.ROOT);
                  if (!"offline".equals(addType) && !"o".equals(addType)) {
                     if (!"cookie".equals(addType) && !"c".equals(addType)) {
                        if (!"browser".equals(addType) && !"b".equals(addType)) {
                           if (!"refresh".equals(addType) && !"r".equals(addType) && !"msa".equals(addType)) {
                              if (!"token".equals(addType) && !"t".equals(addType) && !"access".equals(addType) && !"jwt".equals(addType)) {
                                 ChatUtil.addFormattedChatMessage(String.format("%sUnknown add kind &o%s&r&r", Valor.CHAT_PREFIX, addType));
                                 return;
                              } else if (args.size() < 4) {
                                 ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add token <&oaccess-token&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                                 return;
                              } else {
                                 StringBuilder tokenBuilder = new StringBuilder(args.get(3));
                                 for (int i = 4; i < args.size(); i++) {
                                    tokenBuilder.append(args.get(i));
                                 }

                                 String rawToken;
                                 if (MicrosoftAuthService.cleanToken(rawToken = tokenBuilder.toString()).isEmpty()) {
                                    ChatUtil.addFormattedChatMessage(String.format("%sEmpty access token.&r", Valor.CHAT_PREFIX));
                                    return;
                                 }

                                 ChatUtil.addFormattedChatMessage(String.format("%sValidating Minecraft access token...&r", Valor.CHAT_PREFIX));
                                 new Thread(
                                       () -> {
                                          try {
                                             MicrosoftAuthService.MinecraftProfile profile = MicrosoftAuthService.loginWithAccessToken(rawToken);
                                             AccountStore.addOrUpdateAccount(
                                                AltAccount.createMicrosoft(profile.username, profile.uuid, profile.accessToken, null, profile.expiresAt)
                                             );
                                             ChatUtil.addFormattedChatMessage(String.format("%sAdded Microsoft account &o%s&r (access token)&r", Valor.CHAT_PREFIX, profile.username));
                                          } catch (Exception e) {
                                             ChatUtil.addFormattedChatMessage(String.format("%sFailed: %s&r", Valor.CHAT_PREFIX, e.getMessage()));
                                          }
                                       },
                                       "valor-alt-addtoken"
                                    )
                                    .start();
                                 return;
                              }
                           } else if (args.size() < 4) {
                              ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add refresh <&orefresh-token&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                              return;
                           } else {
                              String refreshToken;
                              if (MicrosoftAuthService.cleanRefreshToken(refreshToken = args.get(3)).isEmpty()) {
                                 ChatUtil.addFormattedChatMessage(String.format("%sEmpty refresh token.&r", Valor.CHAT_PREFIX));
                                 return;
                              }

                              ChatUtil.addFormattedChatMessage(String.format("%sExchanging refresh token for Minecraft session...&r", Valor.CHAT_PREFIX));
                              new Thread(
                                    () -> {
                                       try {
                                          MicrosoftAuthService.RefreshResult refreshResult = MicrosoftAuthService.loginWithRefreshToken(refreshToken);
                                          AccountStore.addOrUpdateAccount(
                                             AltAccount.createMicrosoft(
                                                refreshResult.profile.username,
                                                refreshResult.profile.uuid,
                                                refreshResult.profile.accessToken,
                                                refreshResult.tokens.refreshToken,
                                                refreshResult.profile.expiresAt
                                             )
                                          );
                                          ChatUtil.addFormattedChatMessage(String.format("%sAdded Microsoft account &o%s&r&r", Valor.CHAT_PREFIX, refreshResult.profile.username));
                                       } catch (Exception e) {
                                          ChatUtil.addFormattedChatMessage(String.format("%sFailed: %s&r", Valor.CHAT_PREFIX, e.getMessage()));
                                       }
                                    },
                                    "valor-alt-addrefresh"
                                 )
                                 .start();
                              return;
                           }
                        } else {
                           if (args.size() < 4) {
                              ChatUtil.addFormattedChatMessage(String.format("%sStarting local auth server...&r", Valor.CHAT_PREFIX));
                              new Thread(() -> {
                                 try {
                                    OAuthLocalServer localServer = new OAuthLocalServer(new AccountCommand$1(this));
                                    String authUrl = localServer.start();
                                    ChatUtil.addFormattedChatMessage(String.format("%sBrowser opened. Log in with email:pass at &o%s&r&r", Valor.CHAT_PREFIX, authUrl));
                                    MicrosoftAuthService.openUrlInBrowser(authUrl);
                                    new Thread(() -> {
                                       try {
                                          Thread.sleep(300000L);
                                       } catch (InterruptedException ignored) {
                                       }
                                       localServer.stop();
                                    }, "valor-alt-browserserver-timeout").start();
                                 } catch (Throwable t) {
                                    ChatUtil.addFormattedChatMessage(String.format("%sCouldn't start auth server: %s&r", Valor.CHAT_PREFIX, t.getMessage()));
                                 }
                              }, "valor-alt-browserstart").start();
                              return;
                           }

                           String authCode = args.get(3);
                           ChatUtil.addFormattedChatMessage(String.format("%sExchanging browser auth code...&r", Valor.CHAT_PREFIX));
                           new Thread(
                                 () -> {
                                    try {
                                       MicrosoftAuthService.OAuthTokens tokens = MicrosoftAuthService.loginWithCode(authCode);
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
                                       ChatUtil.addFormattedChatMessage(String.format("%sAdded Microsoft account &o%s&r&r", Valor.CHAT_PREFIX, profile.username));
                                    } catch (Exception e) {
                                       ChatUtil.addFormattedChatMessage(String.format("%sBrowser login failed: %s&r", Valor.CHAT_PREFIX, e.getMessage()));
                                    }
                                 },
                                 "valor-alt-addbrowser"
                              )
                              .start();
                           return;
                        }
                     } else if (args.size() < 4) {
                        ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add cookie <&ofile-path&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                        return;
                     } else {
                        String cookiePath = args.get(3);
                        File cookieFile = new File(cookiePath);
                        if (!cookieFile.exists()) {
                           ChatUtil.addFormattedChatMessage(String.format("%sFile not found: &o%s&r&r", Valor.CHAT_PREFIX, cookiePath));
                           return;
                        }

                        ChatUtil.addFormattedChatMessage(String.format("%sAuthorizing with cookies...&r", Valor.CHAT_PREFIX));
                        new Thread(
                              () -> {
                                 try {
                                    String cookieContent = new String(Files.readAllBytes(cookieFile.toPath()), StandardCharsets.UTF_8);
                                    MicrosoftAuthService.OAuthTokens tokens = MicrosoftAuthService.loginWithCookies(cookieContent);
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
                                    ChatUtil.addFormattedChatMessage(String.format("%sAdded Microsoft account &o%s&r&r", Valor.CHAT_PREFIX, profile.username));
                                 } catch (Exception e) {
                                    ChatUtil.addFormattedChatMessage(String.format("%sCookie login failed: %s&r", Valor.CHAT_PREFIX, e.getMessage()));
                                 }
                              },
                              "valor-alt-addcookie"
                           )
                           .start();
                        return;
                     }
                  } else {
                     if (args.size() < 4) {
                        ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s add offline <&oname&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                        return;
                     }

                     AccountStore.addOrUpdateAccount(AltAccount.createOffline(args.get(3)));
                     ChatUtil.addFormattedChatMessage(String.format("%sAdded offline account &o%s&r&r", Valor.CHAT_PREFIX, args.get(3)));
                     return;
                  }
               }

            case "r":
            case "remove":
            case "delete":
               if (args.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s remove <&oindex&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                  return;
               } else {
                  int removeIndex = parseIndex(args.get(2));
                  AltAccount accountToRemove = AccountStore.getAccount(removeIndex);
                  if (accountToRemove == null) {
                     ChatUtil.addFormattedChatMessage(String.format("%sNo account at index &o%s&r&r", Valor.CHAT_PREFIX, args.get(2)));
                     return;
                  }

                  AccountStore.removeAccount(removeIndex);
                  ChatUtil.addFormattedChatMessage(String.format("%sRemoved &o%s&r&r", Valor.CHAT_PREFIX, accountToRemove.username));
                  return;
               }

            case "refresh":
               if (args.size() < 3) {
                  ChatUtil.addFormattedChatMessage(String.format("%sUsage: .%s refresh <&oindex&r>&r", Valor.CHAT_PREFIX, args.get(0)));
                  return;
               } else {
                  AltAccount accountToRefresh = AccountStore.getAccount(parseIndex(args.get(2)));
                  if (accountToRefresh == null) {
                     ChatUtil.addFormattedChatMessage(String.format("%sNo account at index &o%s&r&r", Valor.CHAT_PREFIX, args.get(2)));
                     return;
                  } else if (accountToRefresh.accountType != AltAccount.AccountType.MICROSOFT) {
                     ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r is not a Microsoft account&r", Valor.CHAT_PREFIX, accountToRefresh.username));
                     return;
                  } else {
                     if (accountToRefresh.refreshToken == null) {
                        ChatUtil.addFormattedChatMessage(String.format("%s&o%s&r has no stored refresh token&r", Valor.CHAT_PREFIX, accountToRefresh.username));
                        return;
                     }

                     ChatUtil.addFormattedChatMessage(String.format("%sRefreshing &o%s&r...&r", Valor.CHAT_PREFIX, accountToRefresh.username));
                     new Thread(() -> {
                        try {
                           MicrosoftAuthService.RefreshResult refreshResult = MicrosoftAuthService.loginWithRefreshToken(accountToRefresh.refreshToken);
                           accountToRefresh.accessToken = refreshResult.profile.accessToken;
                           accountToRefresh.refreshToken = refreshResult.tokens.refreshToken != null ? refreshResult.tokens.refreshToken : accountToRefresh.refreshToken;
                           accountToRefresh.expiresAt = refreshResult.profile.expiresAt;
                           AccountStore.save();
                           ChatUtil.addFormattedChatMessage(String.format("%sRefreshed &o%s&r (token valid for ~24h)&r", Valor.CHAT_PREFIX, accountToRefresh.username));
                        } catch (Exception e) {
                           ChatUtil.addFormattedChatMessage(String.format("%sRefresh failed: %s&r", Valor.CHAT_PREFIX, e.getMessage()));
                        }
                     }, "valor-alt-refresh").start();
                     return;
                  }
               }

            case "active":
               AltAccount currentActive = AccountStore.getActiveAccount();
               if (currentActive == null) {
                  ChatUtil.addFormattedChatMessage(String.format("%sNo active alt&r", Valor.CHAT_PREFIX));
                  return;
               }

               ChatUtil.addFormattedChatMessage(String.format("%sActive: &o%s&r (&o%s&r)&r", Valor.CHAT_PREFIX, currentActive.username, currentActive.accountType.name()));
               return;

            default:
               sendUsage(args.get(0));
         }
      }
   }

   public AccountCommand() {
      super(new ArrayList<>(Arrays.asList("alt", "alts", "account", "accounts")));
   }
}
