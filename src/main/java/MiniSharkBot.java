import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MiniSharkBot extends AbilityBot {
    //public Collection<AbilityExtension> extensionCollections = new ArrayList<>();
    public config config;

    public MiniSharkBot(config config) {
        super(config.getBotToken(), config.getBotUsername());
        this.config = config;
        //extensionCollections.add(new Hello(silent));
        //addExtensions(extensionCollections);
    }

    @Override
    public long creatorId() {
        return 1468601719;
    }

    @Override
    public void onUpdateReceived(Update update) {
        /*
        if (update.hasMessage() && update.getMessage().hasText()) {
            SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText(update.getMessage().getText());
            Crawler crawler = new Crawler(new SQLiter());
            crawler.addCookie("PHPSESSID", cookie);
            try {
                sendPictureMessage(crawler.resolveListPage(startPage + dateUrl), chatID);
            } catch (TelegramApiException | InterruptedException e) {
                e.printStackTrace();
            }
        }
         */
    }

    public void sendButtonMessage(String chatID, InputFile photo) throws TelegramApiException {
        SendPhoto snd = new SendPhoto();
        snd.setPhoto(photo);
        snd.setChatId(chatID);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> Buttons = new ArrayList<>();

        for (AD ad : config.getADs()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(ad.str);
            inlineKeyboardButton.setUrl(ad.url);
            Buttons.add(inlineKeyboardButton);
        }

        keyboard.add(Buttons);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        snd.setReplyMarkup(inlineKeyboardMarkup);
        execute(snd);
    }

    public void sendPictureMessage(List<InputFile> photos, String chatID) throws TelegramApiException, InterruptedException, IOException {
        SendMediaGroup snd = new SendMediaGroup();
        System.out.println(Main.MiniSharkInfo + "开始发送图片至 " + "@" + chatID);
        snd.setChatId("@" + chatID);
        List<InputMedia> inputMedia = new ArrayList<>();
        for (InputFile photo : photos) {
            if (photo.getNewMediaStream().available() > 10485759) {
                System.out.println(Main.MiniSharkInfo + "图片太几把大了跳过");
            }
            if (inputMedia.size() < 10) {
                if (photos.size() - 1 == photos.indexOf(photo)) {
                    executeSendPictureMessage(snd, inputMedia);
                    sendButtonMessage("@" + chatID, photo);
                    return;
                }
                inputMedia.add(new InputMediaPhoto(
                        photo.getAttachName(), null,
                        null, null, true,
                        photo.getMediaName(), null, photo.getNewMediaStream()));
            } else {
                executeSendPictureMessage(snd, inputMedia);
                inputMedia.clear();
                inputMedia.add(new InputMediaPhoto(
                        photo.getAttachName(), null,
                        null, null, true,
                        photo.getMediaName(), null, photo.getNewMediaStream()));
            }
        }
        if (!inputMedia.isEmpty()) {
            snd.setMedias(inputMedia);
            execute(snd);
            System.out.println(Main.MiniSharkInfo + "这波图片发完了休眠60s");
            Thread.sleep(60000);
            inputMedia.clear();
        }
    }

    public void executeSendPictureMessage(SendMediaGroup snd, List<InputMedia> inputMedia) throws TelegramApiException, InterruptedException {
        snd.setMedias(inputMedia);
        execute(snd);
        System.out.println(Main.MiniSharkInfo + "这波图片发完了休眠60s");
        Thread.sleep(60000);
    }
}