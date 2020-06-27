package net.runelite.client.plugins.sotetseg;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.socket.socket.CWSClient;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@PluginDescriptor(
        name = "Sotetseg (Extended)",
        description = "Extended plugin handler for Sotetseg in the Theatre of Blood.",
        tags = {"socket", "server", "discord", "connection", "broadcast", "sotetseg", "theatre", "tob"},
        enabledByDefault = false
)
@Slf4j
public class SotetsegPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private EventBus eventBus;

    @Inject
    private SotetsegConfig config;

    @Inject
    private CWSClient cwsClient;

    @Provides
    SotetsegConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SotetsegConfig.class);
    }

    @Inject
    private SotetsegOverlay overlay;

    // This boolean states whether or not the room is currently active.
    @Getter(AccessLevel.PUBLIC)
    private boolean sotetsegActive;

    // This NPC represents the boss.
    private NPC sotetsegNPC;

    // This represents the bad tiles.
    private LinkedHashSet<Point> redTiles;

    @Getter(AccessLevel.PUBLIC)
    private Set<WorldPoint> mazePings;

    // This represents the amount of times to send data.
    private int dispatchCount;

    // This represents the state of the raid.
    private boolean wasInUnderworld;
    private int overworldRegionID;

    @Override
    protected void startUp() {
        this.sotetsegActive = false;
        this.sotetsegNPC = null;

        this.redTiles = new LinkedHashSet<>();
        this.mazePings = Collections.synchronizedSet(new HashSet<>());

        this.dispatchCount = 5;
        this.wasInUnderworld = false;
        this.overworldRegionID = -1;

        this.overlayManager.add(this.overlay);
        cwsClient.registerMessage(MazePing.class);
    }

    @Override
    protected void shutDown() {
        this.overlayManager.remove(this.overlay);
        cwsClient.unregisterMessage(MazePing.class);
    }

    @Subscribe // Boss has entered the scene. Played has entered the room.
    public void onNpcSpawned(NpcSpawned event) {
        final NPC npc = event.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
                this.sotetsegActive = true;
                this.sotetsegNPC = npc;
                break;
            default:
                break;
        }
    }

    @Subscribe // Boss has left the scene. Player left, died, or the boss was killed.
    public void onNpcDespawned(NpcDespawned event) {
        final NPC npc = event.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
                if (this.client.getPlane() != 3) {
                    this.sotetsegActive = false;
                    this.sotetsegNPC = null;
                }

                break;
            default:
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.sotetsegActive) {
            final Player player = this.client.getLocalPlayer();

            // This check resets all the data if sotetseg is attackable.
            if (this.sotetsegNPC != null && this.sotetsegNPC.getId() == 8388) {
                this.redTiles.clear();
                this.mazePings.clear();
                this.dispatchCount = 5;

                if (this.isInOverWorld()) { // Set the overworld flags.
                    this.wasInUnderworld = false;
                    if (player != null && player.getWorldLocation() != null) {
                        WorldPoint wp = player.getWorldLocation();
                        this.overworldRegionID = wp.getRegionID();
                    }
                }
            }

            if (!this.redTiles.isEmpty() && this.wasInUnderworld) {
                if (this.dispatchCount > 0) { // Ensure we only send the data a couple times.
                    this.dispatchCount--;

                    MazePing mazePing = new MazePing(new MazePing.MazeTile[redTiles.size()]);

                    int i = 0;
                    for (final Point p : this.redTiles) {
                        WorldPoint wp = this.translateMazePoint(p);

                        mazePing.getMazeTiles()[i] = new MazePing.MazeTile(wp.getX(), wp.getY(), wp.getPlane());
                        i++;
                    }

                    cwsClient.sendEndToEndEncrypted(mazePing);
                }
            }
        }
    }

    @Subscribe
    public void onMazePing(MazePing mazePing) {

        mazePings.clear();
        for (MazePing.MazeTile mazeTile : mazePing.getMazeTiles())
        {
                WorldPoint wp = new WorldPoint(mazeTile.getX(), mazeTile.getY(), mazeTile.getPlane());
                mazePings.add(wp);
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        if (this.sotetsegActive) {
            GroundObject o = event.getGroundObject();
            if (o.getId() == 33035) {
                final Tile t = event.getTile();
                final WorldPoint p = WorldPoint.fromLocal(this.client, t.getLocalLocation());
                final Point point = new Point(p.getRegionX(), p.getRegionY());

                if (this.isInOverWorld()) {  // (9, 22) are magical numbers that represent the overworld maze offset.
                    this.redTiles.add(new Point(point.getX() - 9, point.getY() - 22));
                }

                if (this.isInUnderWorld()) {   // (42, 31) are magical numbers that represent the underworld maze offset.
                    this.redTiles.add(new Point(point.getX() - 42, point.getY() - 31));
                    this.wasInUnderworld = true;
                }
            }
        }
    }

    /**
     * Returns whether or not the current player is in the overworld.
     *
     * @return In the overworld?
     */
    private boolean isInOverWorld() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == 13123;
    }

    /**
     * Returns whether or not the current player is in the underworld.
     *
     * @return In the underworld?
     */
    private boolean isInUnderWorld() {
        return this.client.getMapRegions().length > 0 && this.client.getMapRegions()[0] == 13379;
    }

    /**
     * Translates a maze point to a WorldPoint.
     *
     * @param mazePoint Point on the maze.
     * @return WorldPoint
     */
    private WorldPoint translateMazePoint(final Point mazePoint) {
        Player p = this.client.getLocalPlayer();

        // (9, 22) are magical numbers that represent the overworld maze offset.
        if (this.overworldRegionID == -1 && p != null) {
            WorldPoint wp = p.getWorldLocation();
            return WorldPoint.fromRegion(wp.getRegionID(), mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
        }

        return WorldPoint.fromRegion(this.overworldRegionID, mazePoint.getX() + 9, mazePoint.getY() + 22, 0);
    }
}
