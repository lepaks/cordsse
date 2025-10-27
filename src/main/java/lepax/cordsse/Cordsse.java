package lepax.cordsse;

import necesse.engine.modLoader.annotations.ModEntry;
import net.arikia.dev.drpc.*;

@ModEntry
public class Cordsse {
    private static boolean initialized = false;
    private static String lastPresence = "";

    public void init() {
        if (!initialized) {
            DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                    .setReadyEventHandler(user ->
                            System.out.println(user.username))
                    .build();

            DiscordRPC.discordInitialize("1432393755515949106", handlers, true);
            initialized = true;

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DiscordRPC.discordClearPresence();
                DiscordRPC.discordShutdown();
            }));
        }

        new Thread(() -> {
            while (true) {
                try {
                    updatePresence();
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "CORDSSE").start();
    }

    private void updatePresence() {
        DiscordRPC.discordRunCallbacks();

        String currentPresence = PlayerPresence.playerIs();

        if (!currentPresence.equals(lastPresence)) {
            lastPresence = currentPresence;
            DiscordRichPresence rich = new DiscordRichPresence.Builder(currentPresence)
                    .setDetails(PlayerPresence.SinglePlayer())
                    .build();
            DiscordRPC.discordUpdatePresence(rich);
        }
    }
}
