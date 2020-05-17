package net.runelite.client.plugins.unclelitetob;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

import static net.runelite.client.plugins.unclelitetob.ULTheatreConstants.*;

@PluginDescriptor(
        name = "UncleLite+ Theatre",
        description = "Ever wanted to hold some Theatre of Blood records? UncleLite+ has got you. We make it easy.",
        tags = {"unclelite", "uncle", "theatre"},
        enabledByDefault = false
)
@Slf4j
public class ULTheatrePlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private ULTheatreConfig config;

    @Provides
    ULTheatreConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(ULTheatreConfig.class);
    }

    private boolean setupBoard;

    @Override
    protected void startUp() {
        this.setupBoard = false;
    }

    @Subscribe
    private void onWidgetLoaded(WidgetLoaded event) {
        if (event.getGroupId() == THEATRE_WIDGET)
            this.setupBoard = true;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.client.getGameState() != GameState.LOGGED_IN) {
            this.setupBoard = false;
            return;
        }

        if (this.setupBoard) {
            this.setupBoard = false;

            int[] roomTimes = this.getRoomTimes();

            // Set the room times.
            for (ULTheatreRooms room : ULTheatreRooms.values())
                setWidgetText(COMP_MAIDEN + (2 * room.getIndex()), this.getTimeString(roomTimes[room.getIndex()]));

            // Set the challenge time.
            int roomTime = this.getRoomSums(roomTimes, ULTheatreRooms.VERZIK.getIndex());
            setWidgetText(COMP_CHALLENGE_TIME, this.getTimeString(roomTime));

            // Set the overall time.
            int overallTime = roomTime + this.config.getPrepTime();
            setWidgetText(COMP_OVERALL_TIME, this.getTimeString(overallTime));

            // Set the player names and deaths.
            String[] names = new String[]{
                    this.config.getPlayerOneName(),
                    this.config.getPlayerTwoName(),
                    this.config.getPlayerThreeName(),
                    this.config.getPlayerFourName(),
                    this.config.getPlayerFiveName()
            };

            int[] deaths = new int[]{
                    this.config.getPlayerOneDeaths(),
                    this.config.getPlayerTwoDeaths(),
                    this.config.getPlayerThreeDeaths(),
                    this.config.getPlayerFourDeaths(),
                    this.config.getPlayerFiveDeaths()
            };

            boolean done = false;
            int totalDeaths = 0;

            for (int i = 0; i < MAX_PLAYERS; i++) {
                if (names[i].isEmpty() || names[i].equalsIgnoreCase("-"))
                    done = true;

                if (done) {
                    setWidgetText(COMP_PLAYER_ONE_NAME + (i * 2), "-");
                    setWidgetText(COMP_PLAYER_ONE_DEATHS + (i * 2), "-");
                } else {
                    setWidgetText(COMP_PLAYER_ONE_NAME + (i * 2), names[i]);
                    setWidgetText(COMP_PLAYER_ONE_DEATHS + (i * 2), Integer.toString(deaths[i]));
                    totalDeaths += deaths[i];
                }
            }

            setWidgetText(COMP_TOTAL_DEATHS, Integer.toString(totalDeaths));
            setWidgetText(COMP_MVP, this.config.getMVP());
        }
    }

    private void setWidgetText(int compId, String text) {
        Widget widget = this.client.getWidget(THEATRE_WIDGET, compId);
        if (widget == null)
            return;

        widget.setText(text);
    }

    private int[] getRoomTimes() {
        int[] times = new int[]{
                this.config.getMaidenTime(),
                this.config.getBloatTime(),
                this.config.getNylocasTime(),
                this.config.getSotetsegTime(),
                this.config.getXarpusTime(),
                this.config.getVerzikTime()
        };

        for (int i = 0; i < times.length; i++)
            if (times[i] <= 0)
                times[i] = 0;

        return times;
    }

    private int getRoomSums(int[] roomTimes, int upUntil) {
        int sum = 0;
        int min = Math.min(upUntil, roomTimes.length - 1);

        for (int i = 0; i <= min; i++)
            sum += roomTimes[i];

        return sum;
    }

    private String getTimeString(int time) {
        if (time <= 0)
            return "0:00";

        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;

        String value = String.format("%d:%2d", minutes, seconds);
        if (hours > 0)
            value = hours + ":" + value;

        return value.replaceAll(" ", "0");
    }

    @Subscribe
    private void onChatMessage(ChatMessage event) {
        if (event.getType() != ChatMessageType.GAMEMESSAGE)
            return;

        String message = event.getMessage();
        if (message == null)
            return;
        else
            message = message.trim();

        MessageNode node = event.getMessageNode();

        for (ULTheatreRooms room : ULTheatreRooms.values()) {
            if (room.getPattern().matcher(message).matches()) {
                int[] roomTimes = this.getRoomTimes();
                int roomIndex = room.getIndex();

                String roomTime = this.getTimeString(roomTimes[roomIndex]);
                String overallTime = this.getTimeString(this.getRoomSums(roomTimes, roomIndex));

                node.setValue(String.format(room.getMessage(), roomTime, overallTime));
                return;
            }
        }

        if (PATTERN_TOTAL.matcher(message).matches()) {
            int overallTime = this.getRoomSums(this.getRoomTimes(), ULTheatreRooms.VERZIK.getIndex());
            overallTime += this.config.getPrepTime();
            node.setValue(String.format(MESSAGE_TOTAL, this.getTimeString(overallTime)));
            return;
        }
    }

}
