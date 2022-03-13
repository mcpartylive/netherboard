package fr.minuskube.netherboard.minestom;

import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.api.PlayerBoard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPlayerBoard implements PlayerBoard<Component, Integer, Component> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MPlayerBoard.class);

    private final Player player;
    private final String subName;
    private Sidebar sidebar;

    private Component name;

    private final Map<Integer, Component> lines = new HashMap<>();

    private boolean deleted = false;

    public MPlayerBoard(Player player, Component name) {
        this(player, null, name);
    }

    public MPlayerBoard(Player player, String name) {
        this(player, null, Component.text(name));
    }

    public MPlayerBoard(Player player, Sidebar sidebar, String name) {
        this(player, sidebar, Component.text(name));
    }

    public MPlayerBoard(Player player, Sidebar sidebar, Component name) {
        this.player = player;
        this.sidebar = sidebar;

        // The sidebar doesn't exist! Create it and add our player as a viewer.
        if (this.sidebar == null) {
            this.sidebar = new Sidebar(name);

            if (!this.sidebar.getViewers().contains(player)) {
                this.sidebar.addViewer(player);
            }
        }

        this.name = name;
        this.subName = player.getUsername().length() <= 8 ? player.getUsername()
            : player.getUsername().substring(0, 8);
    }

    @Override
    public Component get(Integer score) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        return this.lines.get(score);
    }

    @Override
    public void set(Component lineText, Integer score) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        Component previousLine = this.lines.get(score);
        if (lineText.equals(previousLine)) return;

        Sidebar.ScoreboardLine scoreboardLine = new Sidebar.ScoreboardLine(this.subName + "_" + score, lineText, score);
        if (previousLine != null) {
            this.sidebar.removeLine(this.subName + "_" + score);
            this.sidebar.createLine(scoreboardLine);
        } else {
            this.sidebar.createLine(scoreboardLine);
        }

        this.lines.put(score, lineText);
    }

    @Override
    public void setAll(Component... lines) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        for (int i = 0; i < lines.length; i++) {
            Component line = lines[i];
            set(line, lines.length - i);
        }

        Set<Integer> scores = new HashSet<>(this.lines.keySet());
        for (Integer score : scores) {
            if (score <= 0 || score > lines.length) {
                remove(score);
            }
        }
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

        Component line = this.lines.get(score);
        if (line == null) return;

        this.sidebar.removeLine(this.subName + "_" + score);
        this.lines.remove(score);
    }

    @Override
    public void delete() {
        if (this.deleted) return;

        Netherboard.instance().removeBoard(sidebar);
        this.sidebar.removeViewer(player);
        // TODO: I feel like this board won't garbage collect nicely,
        //       However, all I can do is remove viewers and hope.
        this.deleted = true;
    }

    @Override
    public Map<Integer, Component> getLines() {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        return new HashMap<>(lines);
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void setName(Component name) {
        if (this.deleted) {
            throw new IllegalStateException("The PlayerBoard is deleted!");
        }

        this.name = name;
    }

    public Player getPlayer() {
        return player;
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

}
