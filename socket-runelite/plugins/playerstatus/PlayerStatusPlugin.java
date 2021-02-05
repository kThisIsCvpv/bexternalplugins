/*
 * Copyright (c) 2017, Seth <Sethtroll3@gmail.com>
 * Copyright (c) 2018, Jordan Atwood <jordan.atwood423@gmail.com>
 * Copyright (c) 2020, Charles Xu <github.com/kthisiscvpv>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.socket.plugins.playerstatus;

import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.plugins.socket.packet.SocketBroadcastPacket;
import net.runelite.client.plugins.socket.packet.SocketPlayerLeave;
import net.runelite.client.plugins.socket.packet.SocketReceivePacket;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameIndicator;
import net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameTimer;
import net.runelite.client.plugins.socket.plugins.playerstatus.marker.AbstractMarker;
import net.runelite.client.plugins.socket.plugins.playerstatus.marker.IndicatorMarker;
import net.runelite.client.plugins.socket.plugins.playerstatus.marker.TimerMarker;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.*;

import static net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameIndicator.VENGEANCE_ACTIVE;
import static net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameTimer.*;
import static net.runelite.client.plugins.socket.plugins.playerstatus.gametimer.GameTimerConstant.*;

@PluginDescriptor(
        name = "Socket - Player Status",
        description = "Socket extension for displaying player status to members in your party.",
        tags = {"socket", "server", "discord", "connection", "broadcast", "player", "status", "venge", "vengeance"},
        enabledByDefault = true
)
@Slf4j
public class PlayerStatusPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private PlayerStatusOverlay overlay;

    @Inject
    private PlayerSidebarOverlay sidebar;

    @Inject
    private PlayerStatusConfig config;

    @Provides
    PlayerStatusConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PlayerStatusConfig.class);
    }

    @Getter(AccessLevel.PUBLIC)
    private Map<String, List<AbstractMarker>> statusEffects = new HashMap<String, List<AbstractMarker>>();

    @Getter(AccessLevel.PUBLIC)
    private Map<String, PlayerStatus> partyStatus = new TreeMap<String, PlayerStatus>();

    private int lastRaidVarb;
    private int lastVengCooldownVarb;
    private int lastIsVengeancedVarb;
    private int lastRefresh;

    @Override
    protected void startUp() {
        this.lastRaidVarb = -1;
        this.lastRefresh = 0;

        synchronized (this.statusEffects) {
            this.statusEffects.clear();
        }

        synchronized (this.partyStatus) {
            this.partyStatus.clear();
        }

        this.overlayManager.add(this.overlay);
        this.overlayManager.add(this.sidebar);
    }

    @Override
    protected void shutDown() {
        this.overlayManager.remove(this.overlay);
        this.overlayManager.remove(this.sidebar);
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int raidVarb = client.getVar(Varbits.IN_RAID);
        int vengCooldownVarb = client.getVar(Varbits.VENGEANCE_COOLDOWN);
        int isVengeancedVarb = client.getVar(Varbits.VENGEANCE_ACTIVE);

        if (lastRaidVarb != raidVarb) {
            removeGameTimer(OVERLOAD_RAID);
            removeGameTimer(PRAYER_ENHANCE);
            lastRaidVarb = raidVarb;
        }

        if (lastVengCooldownVarb != vengCooldownVarb) {
            if (vengCooldownVarb == 1) {
                createGameTimer(VENGEANCE);
            } else {
                removeGameTimer(VENGEANCE);
            }

            lastVengCooldownVarb = vengCooldownVarb;
        }

        if (lastIsVengeancedVarb != isVengeancedVarb) {
            if (isVengeancedVarb == 1) {
                createGameIndicator(VENGEANCE_ACTIVE);
            } else {
                removeGameIndicator(VENGEANCE_ACTIVE);
            }

            lastIsVengeancedVarb = isVengeancedVarb;
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().contains("Drink")
                && (event.getId() == ItemID.STAMINA_MIX1
                || event.getId() == ItemID.STAMINA_MIX2)) {
            // Needs menu option hook because mixes use a common drink message, distinct from their standard potion messages
            createGameTimer(STAMINA);
            return;
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        if (event.getMessage().equals(STAMINA_DRINK_MESSAGE) || event.getMessage().equals(STAMINA_SHARED_DRINK_MESSAGE)) {
            createGameTimer(STAMINA);
        }

        if (event.getMessage().equals(STAMINA_EXPIRED_MESSAGE)) {
            removeGameTimer(STAMINA);
        }

        if (event.getMessage().startsWith("You drink some of your") && event.getMessage().contains("overload")) {
            if (client.getVar(Varbits.IN_RAID) == 1) {
                createGameTimer(OVERLOAD_RAID);
            } else {
                createGameTimer(OVERLOAD);
            }

        }

        if (event.getMessage().startsWith("You drink some of your") && event.getMessage().contains("prayer enhance")) {
            createGameTimer(PRAYER_ENHANCE);
        }

        if (event.getMessage().equals(IMBUED_HEART_READY_MESSAGE)) {
            removeGameTimer(IMBUED_HEART);
        }
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        Actor actor = event.getActor();

        if (actor != client.getLocalPlayer()) {
            return;
        }

        if (actor.getGraphic() == IMBUED_HEART.getGraphicId()) {
            createGameTimer(IMBUED_HEART);
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (event.getActor() != this.client.getLocalPlayer())
            return;

        synchronized (this.statusEffects) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(null);
            if (activeEffects == null)
                return;

            for (AbstractMarker marker : new ArrayList<AbstractMarker>(activeEffects)) {
                if (marker instanceof TimerMarker) {
                    TimerMarker timer = (TimerMarker) marker;
                    if (timer.getTimer().isRemovedOnDeath())
                        activeEffects.remove(marker);
                }
            }

            if (activeEffects.isEmpty())
                this.statusEffects.remove(null);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        switch (event.getGameState()) {
            case HOPPING:
            case LOGIN_SCREEN:
            case LOGIN_SCREEN_AUTHENTICATOR: {
                synchronized (this.statusEffects) { // Remove all party member trackers after you log out.
                    for (String s : new ArrayList<String>(this.statusEffects.keySet()))
                        if (s != null) // s == null is local player, so we ignore
                            this.statusEffects.remove(s);
                }

                synchronized (this.partyStatus) {
                    this.partyStatus.clear();
                }

                break;
            }

            default:
                break;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.client.getGameState() != GameState.LOGGED_IN)
            return;

        int currentHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int currentPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
        int maxHealth = client.getRealSkillLevel(Skill.HITPOINTS);
        int maxPrayer = client.getRealSkillLevel(Skill.PRAYER);
        int specialAttack = this.client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10; // This variable is in [0, 1000]. So we divide by 10.
        int runEnergy = this.client.getEnergy();

        String name = this.client.getLocalPlayer().getName();

        PlayerStatus status;
        synchronized (this.partyStatus) {
            status = this.partyStatus.get(name);
            if (status == null) {
                status = new PlayerStatus(currentHealth, maxHealth, currentPrayer, maxPrayer, runEnergy, specialAttack);
                this.partyStatus.put(name, status);
            } else {
                status.setHealth(currentHealth);
                status.setMaxHealth(maxHealth);
                status.setPrayer(currentPrayer);
                status.setMaxPrayer(maxPrayer);
                status.setRun(runEnergy);
                status.setSpecial(specialAttack);
            }
        }

        this.lastRefresh++;
        if (this.lastRefresh >= Math.max(1, this.config.getStatsRefreshRate())) {
            JSONObject packet = new JSONObject();
            packet.put("name", name);
            packet.put("player-stats", status.toJSON());
            this.eventBus.post(new SocketBroadcastPacket(packet));
            this.lastRefresh = 0;
        }
    }

    private void sortMarkers(List<AbstractMarker> markers) {
        markers.sort(new Comparator<AbstractMarker>() {
            @Override
            public int compare(AbstractMarker o1, AbstractMarker o2) {
                return Integer.compare(getMarkerOrdinal(o1), getMarkerOrdinal(o2));
            }

            private int getMarkerOrdinal(AbstractMarker marker) {
                if (marker == null)
                    return -1;

                if (marker instanceof IndicatorMarker)
                    return ((IndicatorMarker) marker).getIndicator().ordinal();

                if (marker instanceof TimerMarker)
                    return ((TimerMarker) marker).getTimer().ordinal();

                return -1;
            }
        });
    }

    private void createGameTimer(GameTimer timer) {
        this.createGameTimer(timer, null);

        JSONObject packet = new JSONObject();
        packet.put("player-status-game-add", this.client.getLocalPlayer().getName());
        packet.put("effect-name", timer.name());

        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    private void createGameTimer(final GameTimer timer, String name) {
        TimerMarker marker = new TimerMarker(timer, System.currentTimeMillis());
        switch (timer.getImageType()) {
            case SPRITE:
                marker.setBaseImage(spriteManager.getSprite(timer.getImageId(), 0));
                break;
            case ITEM:
                marker.setBaseImage(itemManager.getImage(timer.getImageId()));
                break;
        }

        removeGameTimer(timer, name);

        synchronized (this.statusEffects) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);

            if (activeEffects == null) {
                activeEffects = new ArrayList<AbstractMarker>();
                this.statusEffects.put(name, activeEffects);
            }

            activeEffects.add(marker);
            this.sortMarkers(activeEffects);
        }
    }

    private void removeGameTimer(GameTimer timer) {
        this.removeGameTimer(timer, null);

        if (this.client.getLocalPlayer() == null)
            return;

        JSONObject packet = new JSONObject();
        packet.put("player-status-game-remove", this.client.getLocalPlayer().getName());
        packet.put("effect-name", timer.name());

        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    private void removeGameTimer(GameTimer timer, String name) {
        synchronized (this.statusEffects) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);
            if (activeEffects == null)
                return;

            for (AbstractMarker marker : new ArrayList<AbstractMarker>(activeEffects)) {
                if (marker instanceof TimerMarker) {
                    TimerMarker instance = (TimerMarker) marker;
                    if (instance.getTimer() == timer)
                        activeEffects.remove(marker);
                }
            }

            if (activeEffects.isEmpty())
                this.statusEffects.remove(name);
        }
    }

    private void createGameIndicator(GameIndicator gameIndicator) {
        this.createGameIndicator(gameIndicator, null);

        if (this.client.getLocalPlayer() == null)
            return;

        JSONObject packet = new JSONObject();
        packet.put("player-status-indicator-add", this.client.getLocalPlayer().getName());
        packet.put("effect-name", gameIndicator.name());

        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    private void createGameIndicator(GameIndicator gameIndicator, String name) {
        IndicatorMarker marker = new IndicatorMarker(gameIndicator);
        switch (gameIndicator.getImageType()) {
            case SPRITE:
                marker.setBaseImage(spriteManager.getSprite(gameIndicator.getImageId(), 0));
                break;
            case ITEM:
                marker.setBaseImage(itemManager.getImage(gameIndicator.getImageId()));
                break;
        }

        removeGameIndicator(gameIndicator, name);

        synchronized (this.statusEffects) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);

            if (activeEffects == null) {
                activeEffects = new ArrayList<AbstractMarker>();
                this.statusEffects.put(name, activeEffects);
            }

            activeEffects.add(marker);
            this.sortMarkers(activeEffects);
        }
    }

    private void removeGameIndicator(GameIndicator indicator) {
        this.removeGameIndicator(indicator, null);

        JSONObject packet = new JSONObject();
        packet.put("player-status-indicator-remove", this.client.getLocalPlayer().getName());
        packet.put("effect-name", indicator.name());

        this.eventBus.post(new SocketBroadcastPacket(packet));
    }

    private void removeGameIndicator(GameIndicator indicator, String name) {
        synchronized (this.statusEffects) {
            List<AbstractMarker> activeEffects = this.statusEffects.get(name);
            if (activeEffects == null)
                return;

            for (AbstractMarker marker : new ArrayList<AbstractMarker>(activeEffects)) {
                if (marker instanceof IndicatorMarker) {
                    IndicatorMarker instance = (IndicatorMarker) marker;
                    if (instance.getIndicator() == indicator)
                        activeEffects.remove(marker);
                }
            }

            if (activeEffects.isEmpty())
                this.statusEffects.remove(name);
        }
    }

    @Subscribe
    public void onSocketReceivePacket(SocketReceivePacket event) {
        try {
            JSONObject payload = event.getPayload();
            String localName = this.client.getLocalPlayer().getName();

            if (payload.has("player-stats")) {
                String targetName = payload.getString("name");
                if (targetName.equals(localName))
                    return;

                JSONObject statusJson = payload.getJSONObject("player-stats");

                PlayerStatus status;
                synchronized (this.partyStatus) {
                    status = this.partyStatus.get(targetName);
                    if (status == null) {
                        status = PlayerStatus.fromJSON(statusJson);
                        this.partyStatus.put(targetName, status);
                    } else
                        status.parseJSON(statusJson);
                }

            } else if (payload.has("player-status-game-add")) {
                String targetName = payload.getString("player-status-game-add");
                if (targetName.equals(localName))
                    return;

                String effectName = payload.getString("effect-name");
                GameTimer timer = GameTimer.valueOf(effectName);
                this.createGameTimer(timer, targetName);

            } else if (payload.has("player-status-game-remove")) {
                String targetName = payload.getString("player-status-game-remove");
                if (targetName.equals(localName))
                    return;

                String effectName = payload.getString("effect-name");
                GameTimer timer = GameTimer.valueOf(effectName);
                this.removeGameTimer(timer, targetName);

            } else if (payload.has("player-status-indicator-add")) {
                String targetName = payload.getString("player-status-indicator-add");
                if (targetName.equals(localName))
                    return;

                String effectName = payload.getString("effect-name");
                GameIndicator indicator = GameIndicator.valueOf(effectName);
                this.createGameIndicator(indicator, targetName);

            } else if (payload.has("player-status-indicator-remove")) {
                String targetName = payload.getString("player-status-indicator-remove");
                if (targetName.equals(localName))
                    return;

                String effectName = payload.getString("effect-name");
                GameIndicator indicator = GameIndicator.valueOf(effectName);
                this.removeGameIndicator(indicator, targetName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onSocketPlayerLeave(SocketPlayerLeave event) {
        String target = event.getPlayerName();

        synchronized (this.statusEffects) {
            if (this.statusEffects.containsKey(target)) {
                this.statusEffects.remove(target);
            }
        }

        synchronized (this.partyStatus) {
            if (this.partyStatus.containsKey(target)) {
                this.partyStatus.remove(target);
            }
        }
    }
}
