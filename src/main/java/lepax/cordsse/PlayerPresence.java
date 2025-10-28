package lepax.cordsse;

import necesse.engine.GlobalData;
import necesse.engine.network.client.Client;
import necesse.engine.state.MainGame;
import necesse.engine.state.State;
import net.arikia.dev.drpc.DiscordRichPresence;

import java.util.Objects;

class PlayerPresence {
    static Client getGameClient() {
        State currentState = GlobalData.getCurrentState();
        if (currentState instanceof MainGame)
            return ((MainGame) currentState).getClient();
        else
            return null;
    }

    public static String playerIs() {
        State currentState = GlobalData.getCurrentState();

        if (currentState instanceof MainGame) {
            Client client = getGameClient();

            if (client != null || client.getPlayer() != null) {
                return String.format("in %s as %s", client.getLocalServer().world.displayName, client.getPlayer().playerName);
            } else {
                return "Game";
            }
        }
        return "Main Menu";
    }

    public static String SinglePlayer() {
        Client client = getGameClient();
        if (client == null) {
            return "";
        } else {
            boolean singlemulti = Objects.requireNonNull(getGameClient()).isSingleplayer();
            if (singlemulti) {
                return "Singleplayer";
            } else {
                return String.format("Multiplayer");
            }
        }
    }
}
