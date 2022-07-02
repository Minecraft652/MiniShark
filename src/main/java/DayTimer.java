import PixivDownloader.Crawler;
import PixivDownloader.SQLiter;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DayTimer extends TimerTask {
    public MiniSharkBot bot;
    public config config;
    public DayTimer(MiniSharkBot bot, config config) {
        this.bot = bot;
        this.config = config;
    }
    @Override
    public void run() {
        if (new Date().toString().contains(config.getTime())) {
            System.out.println(Main.MiniSharkInfo + "午时已到, 正在初始化...");
            Crawler crawler = new Crawler(new SQLiter());
            crawler.addCookie("PHPSESSID", config.getCookie());
            try {
                List<InputFile> pictures = crawler.resolveListPage(config.getStartPageUrl() + config.getDateUrl());
                for (String chatID : config.getChatID()) {
                    bot.sendPictureMessage(pictures, chatID);
                    System.out.println(Main.MiniSharkInfo + "这个频道发完了休眠60s");
                    Thread.sleep(60000);
                }
                config.setDateUrl(getNextDay(config.getDateUrl()));
                System.out.println(Main.MiniSharkInfo + "该周期运行完成");
                System.out.println(Main.MiniSharkInfo + "下一日期: " + config.getDateUrl());
            } catch (TelegramApiException | InterruptedException | ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<InputFile> copyList(List<InputFile> list) {
        return new ArrayList<>(list);
    }

    private String getNextDay(String dateTime) throws ParseException {
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd");
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(simpleDateFormat.parse(dateTime));
        calendar.add(Calendar.DAY_OF_MONTH,+1);
        return simpleDateFormat.format(calendar.getTime());
    }
}