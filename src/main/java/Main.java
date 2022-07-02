import Abilities.Hello;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    public static config config;
    public final static String MiniSharkInfo = "[MiniShark] ";
    public static void main(String[] args) {
        try {
            File configFile = new File("config.yml");
            if (!configFile.exists()) {
                Files.copy(Objects.requireNonNull(Main.class.getResourceAsStream("config.yml")), configFile.toPath());
            }
            Yaml yaml = new Yaml();
            FileInputStream in = new FileInputStream(configFile);
            config = yaml.loadAs(in, config.class);
            MiniSharkBot miniSharkBot = new MiniSharkBot(config);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(miniSharkBot);
            Timer timer = new Timer();
            timer.schedule(new DayTimer(miniSharkBot, config), 500, 500);
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
        }
    }
}
