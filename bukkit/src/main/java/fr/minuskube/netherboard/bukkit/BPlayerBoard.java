package fr.minuskube.netherboard.bukkit;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.api.PlayerBoard;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.ServerScoreboard.Method;
import net.minecraft.world.scores.Score;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BPlayerBoard implements PlayerBoard<String, Integer, Component> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BPlayerBoard.class);

    private final Player player;
    private Scoreboard scoreboard;

    private Component name;

    private Objective objective;
    private Objective buffer;

    private Map<Integer, String> lines = new HashMap<>();

    private boolean deleted = false;

    public BPlayerBoard(Player player, Component name) {
        this(player, null, name);
    }

    public BPlayerBoard(Player player, Scoreboard scoreboard, Component name) {
        this.player = player;
        this.scoreboard = scoreboard;

        if(this.scoreboard == null) {
            this.scoreboard = player.getScoreboard();
        }

        this.name = name;

        String subName = player.getName().length() <= 14
                ? player.getName()
                : player.getName().substring(0, 14);

        objective = this.scoreboard.getObjective("sb" + subName);
        buffer = this.scoreboard.getObjective("bf" + subName);

        if(objective == null) {
            objective = this.scoreboard.registerNewObjective("sb" + subName, "dummy", name);
        }
        if(buffer == null) {
            buffer = this.scoreboard.registerNewObjective("bf" + subName, "dummy", name);
        }

        objective.displayName(name);
        sendObjective(objective, ObjectiveMode.CREATE);
        sendObjectiveDisplay(objective);

        buffer.displayName(name);
        sendObjective(buffer, ObjectiveMode.CREATE);

        this.player.setScoreboard(this.scoreboard);
    }

    @Override
    public String get(Integer score) {
        if(deleted) throw new IllegalStateException("The PlayerBoard is deleted!");

        return lines.get(score);
    }

    @Override
    public void set(String name, Integer score) {
        if(deleted) throw new IllegalStateException("The PlayerBoard is deleted!");

        String oldName = lines.get(score);

        if(name.equals(oldName)) return;

        lines.entrySet()
            .removeIf(entry -> entry.getValue().equals(name));

        if(oldName != null) {
            sendScore(buffer, oldName, score, true);
            sendScore(buffer, name, score, false);

            swapBuffers();

            sendScore(buffer, oldName, score, true);
            sendScore(buffer, name, score, false);
        } else {
            sendScore(objective, name, score, false);
            sendScore(buffer, name, score, false);
        }

        lines.put(score, name);
    }

    @Override
    public void setAll(String... lines) {
        if(deleted) throw new IllegalStateException("The PlayerBoard is deleted!");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            set(line, lines.length - i);
        }

        this.lines.keySet().forEach(score -> { // This could also be a stream...
            if (score <= 0 || score > lines.length) {
                remove(score);
            }
        });
    }

    @Override
    public void clear() {
        lines.keySet().forEach(this::remove);
        lines.clear(); // "Optional operation", keeping it for now.
    }

    private void swapBuffers() {
        sendObjectiveDisplay(buffer);

        Objective temp = buffer;

        buffer = objective;
        objective = temp;
    }

    private void sendObjective(Objective obj, ObjectiveMode mode) {
        ClientboundSetObjectivePacket packet = new ClientboundSetObjectivePacket(
            (net.minecraft.world.scores.Objective) obj,
            mode.ordinal()
        );

        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().connection.send(packet);
    }

    private void sendObjectiveDisplay(Objective obj) {
        ClientboundSetDisplayObjectivePacket packet = new ClientboundSetDisplayObjectivePacket(
            1,
            (net.minecraft.world.scores.Objective) obj
        );

        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().connection.send(packet);
    }

    private void sendScore(Objective obj, String name, int score, boolean remove) {
        net.minecraft.world.scores.Scoreboard scoreboard = (net.minecraft.world.scores.Scoreboard) this.scoreboard;
        net.minecraft.world.scores.Objective objective = (net.minecraft.world.scores.Objective) obj;

        Score sbScore = new Score(scoreboard, objective, name);
        sbScore.setScore(score);

        ClientboundSetScorePacket packet = new ClientboundSetScorePacket(
            remove ? Method.REMOVE : Method.CHANGE,
            obj.getName(),
            name,
            score
        );

        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().connection.send(packet);
    }

    @Override
    public void remove(Integer score) {
        if(deleted) throw new IllegalStateException("The PlayerBoard is deleted!");

        String name = lines.get(score);

        if(name == null) return;

        scoreboard.resetScores(name);
        lines.remove(score);
    }

    @Override
    public void delete() {
        if(deleted) return;

        Netherboard.instance().removeBoard(player);

        sendObjective(objective, ObjectiveMode.REMOVE);
        sendObjective(buffer, ObjectiveMode.REMOVE);

        objective.unregister();
        objective = null;

        buffer.unregister();
        buffer = null;

        lines = null;

        deleted = true;
    }

    @Override
    public Map<Integer, String> getLines() {
        if(deleted) throw new IllegalStateException("The PlayerBoard is deleted!");

        return lines;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void setName(Component name) {
        if(deleted) throw new IllegalStateException("The PlayerBoard is deleted!");

        this.name = name;

        objective.displayName(name);
        buffer.displayName(name);

        sendObjective(objective, ObjectiveMode.UPDATE);
        sendObjective(buffer, ObjectiveMode.UPDATE);
    }

    public Player getPlayer() {
        return player;
    }

    public Scoreboard getScoreboard() { return scoreboard; }


    private enum ObjectiveMode { CREATE, REMOVE, UPDATE }

}
