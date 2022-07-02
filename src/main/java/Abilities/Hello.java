package Abilities;

import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.abilitybots.api.util.AbilityExtension;

import static org.telegram.abilitybots.api.objects.Locality.*;
import static org.telegram.abilitybots.api.objects.Privacy.*;

public class Hello implements AbilityExtension {
    public SilentSender silent;
    public Hello(SilentSender silent) {
        this.silent = silent;
    }
    public Ability hello() {
        return Ability.builder()
                .name("hello")
                .info("says hello world!")
                .input(0)
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("Hello world!", ctx.chatId()))
                .post(ctx -> silent.send("Bye world!", ctx.chatId()))
                .build();
    }
}
