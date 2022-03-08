package fr.minuskube.netherboard.minestom;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.api.PlayerBoard;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Scoreboard;
import net.minestom.server.scoreboard.Sidebar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPlayerBoard implements PlayerBoard<String, Integer, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MPlayerBoard.class);

    private final Player player;
    private Scoreboard scoreboard;

    private String name;

    private final Map<Integer, String> lines = new HashMap<>();

    private boolean deleted = false;

    public MPlayerBoard(Player player, String name) {
        this(player, null, name);
    }

    public MPlayerBoard(Player player, Scoreboard scoreboard, String name) {
        this.player = player;
        this.scoreboard = scoreboard;

        if (this.scoreboard == null) {
            Scoreboard sb = new Sidebar(name);

            if (!sb.getViewers().contains(player)) {
                sb.addViewer(player);
            }

            this.scoreboard = sb;
        }

        this.name = name;

        String subName = player.getUsername().length() <= 14
            ? player.getUsername()
            : player.getUsername().substring(0, 14);
    }

    @Override
    public String get(Integer score) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        return this.lines.get(score);
    }

    @Override
    public void set(String name, Integer score) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        String oldName = this.lines.get(score);

        throw new UnsupportedOperationException("set not implemented for Minestom");

        /*
        if(name.equals(oldName))
            return;

        if(oldName != null) {
            this.buffer.removeScore(oldName);
            this.buffer.getOrCreateScore(name).setScore(score);

            swapBuffers();

            this.buffer.removeScore(oldName);
            this.buffer.getOrCreateScore(name).setScore(score);
        }
        else {
            this.objective.getOrCreateScore(name).setScore(score);
            this.buffer.getOrCreateScore(name).setScore(score);
        }

        this.lines.put(score, name);*/
    }

    @Override
    public void setAll(String... lines) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        throw new UnsupportedOperationException("setAll not implemented for Minestom");
        /*
        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];

            set(line, lines.length - i);
        }

        Set<Integer> scores = new HashSet<>(this.lines.keySet());
        for (int score : scores) {
            if (score <= 0 || score > lines.length) {
                remove(score);
            }
        }*/
    }

    @Override
    public void clear() {
        new HashSet<>(this.lines.keySet()).forEach(this::remove);
        this.lines.clear();
    }

    @Override
    public void remove(Integer score) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        throw new UnsupportedOperationException("remove not implemented for Minestom");
        /*
        String name = this.lines.get(score);

        if(name == null) {
            return;
        }

        this.scoreboard.lin(name);
        this.lines.remove(score); */
    }

    @Override
    public void delete() {
        if (this.deleted) return;

        Netherboard.instance().removeBoard(scoreboard);
        this.scoreboard.removeViewer(player);
        // TODO: I feel like this board won't garbage collect nicely,
        //       However, all I can do is remove viewers and hope.
        this.deleted = true;
    }

    @Override
    public Map<Integer, String> getLines() {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        return new HashMap<>(lines);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        this.name = name;

    }

    public Player getPlayer() {
        return player;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

}
