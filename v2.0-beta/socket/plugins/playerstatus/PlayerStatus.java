package net.runelite.client.plugins.socket.plugins.playerstatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.socket.org.json.JSONObject;
import net.runelite.client.ui.overlay.components.PanelComponent;

@Setter(AccessLevel.PUBLIC)
@Getter(AccessLevel.PUBLIC)
@AllArgsConstructor
public class PlayerStatus {

    private final PanelComponent panel = new PanelComponent();

    private int health;
    private int maxHealth;

    private int prayer;
    private int maxPrayer;

    private int run;
    private int special;

    private PlayerStatus() {}

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("health", this.health);
        json.put("max-health", this.maxHealth);
        json.put("prayer", this.prayer);
        json.put("max-prayer", this.maxPrayer);
        json.put("run", this.run);
        json.put("special", this.special);
        return json;
    }

    public void parseJSON(JSONObject json) {
        this.health = json.getInt("health");
        this.maxHealth = json.getInt("max-health");
        this.prayer = json.getInt("prayer");
        this.maxPrayer = json.getInt("max-prayer");
        this.run = json.getInt("run");
        this.special = json.getInt("special");
    }

    public static PlayerStatus fromJSON(JSONObject json) {
        PlayerStatus ps = new PlayerStatus();
        ps.parseJSON(json);
        return ps;
    }
}
