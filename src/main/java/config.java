import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class config {
    private String botToken;
    private String botUsername;
    private String cookie;
    private String dateUrl;
    private String telegramUrl;
    private String startPageUrl;
    private String time;
    private List<String> chatID;
    private ArrayList<AD> ADs;
    public config() {}

    public List<String> getChatID() {
        return chatID;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getCookie() {
        return cookie;
    }

    public String getDateUrl() {
        return dateUrl;
    }

    public String getStartPageUrl() {
        return startPageUrl;
    }

    public String getTime() {
        return time;
    }

    public String getTelegramUrl() {
        return telegramUrl;
    }

    public ArrayList<AD> getADs() {
        return ADs;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }

    public void setChatID(List<String> chatID) {
        this.chatID = chatID;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setDateUrl(String dateUrl) {
        this.dateUrl = dateUrl;
    }

    public void setStartPageUrl(String startPageUrl) {
        this.startPageUrl = startPageUrl;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTelegramUrl(String telegramUrl) {
        this.telegramUrl = telegramUrl;
    }

    public void setADs(ArrayList<AD> ADs) {
        this.ADs = ADs;
    }
}
