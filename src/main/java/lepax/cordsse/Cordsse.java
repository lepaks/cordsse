package lepax.cordsse;

import necesse.engine.modLoader.annotations.ModEntry;
import net.arikia.dev.drpc.*;

import static lepax.cordsse.PlayerPresence.getGameClient;

@ModEntry
public class Cordsse {
    private static boolean initialized = false;
    private static volatile boolean running = true;
    private static String lastSignature = "";

    public void init() {
        if (!initialized) {
            DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                    .setReadyEventHandler(user -> System.out.println(user.username))
                    .build();

            DiscordRPC.discordInitialize("1432393755515949106", handlers, true);
            initialized = true;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { running = false; } catch (Throwable ignored) {}
                try { DiscordRPC.discordClearPresence(); } catch (Throwable ignored) {}
                try { DiscordRPC.discordShutdown(); } catch (Throwable ignored) {}
            }, "CORDSSE-SHUTDOWN"));
        }

        Thread rpcThread = getThread();
        rpcThread.start();
    }

    private Thread getThread() {
        Thread rpcThread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    updatePresence();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try { DiscordRPC.discordClearPresence(); } catch (Throwable ignored) {}
            try { DiscordRPC.discordShutdown(); } catch (Throwable ignored) {}
        }, "CORDSSE");
        rpcThread.setDaemon(true);
        return rpcThread;
    }

    private void updatePresence() {
        DiscordRPC.discordRunCallbacks();

        String state = PlayerPresence.playerIs();
        if (state.isEmpty()) return;

        String details = PlayerPresence.SinglePlayer();

        String bigKey;
        String bigText;

        boolean inMenu = (getGameClient() == null) || (getGameClient().getLevel() == null);
        if (inMenu) {
            bigKey = "icon";
            bigText = "In Menu";
        } else {
            boolean isCave = getGameClient().getLevel().isCave;
            if (isCave) {
                bigKey = "cave";
                bigText = "In Cave";
            } else {
                bigKey = "surface";
                bigText = "On Surface";
            }
        }

        String playerIcon = "icon";
        String playerHealth = "";

        if (!inMenu && getGameClient() != null && getGameClient().getPlayer() != null) {
            int hp = getGameClient().getPlayer().getHealth();
            playerHealth = hp + " HP";
        }

        String signature = state + "|" + details + "|" + bigKey + "|" + bigText + "|" + playerHealth;
        if (signature.equals(lastSignature)) return;
        lastSignature = signature;

        DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(state)
                .setDetails(details)
                .setBigImage(bigKey, bigText);

        if (!inMenu) {
            builder.setSmallImage(playerIcon, playerHealth);
        }

        DiscordRichPresence rich = builder.build();
        DiscordRPC.discordUpdatePresence(rich);
    }
}
