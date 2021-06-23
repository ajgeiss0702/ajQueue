import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.PatternReplacementResult;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RegexTest {
    @Test
    public void regex() throws Exception {
        Component msg = Component.text("&b{SERVER} &7({COUNT}): {LIST}");
        Component playerList = Component.empty();
        List<String> players = Arrays.asList("bot01", "bot02", "bot03", "bot04", "bot05", "bot06");
        for(String p : players) {
            playerList = playerList.append(Component.text("&9"+p+"&7, "));
        }
        Component finalPlayerList = playerList;
        msg = msg.replaceText(b -> b.match(Pattern.compile("\\{LIST}")).replacement(finalPlayerList));
        char[] commaCountString = PlainTextComponentSerializer.plainText().serialize(msg).toCharArray();
        int commas = 0;
        for(Character fChar : commaCountString) {
            if(fChar == ',') commas++;
        }

        int finalCommas = commas;
        msg = msg.replaceText(b -> b.match(",(?!.*,)").replacement("").condition((r, c, re) -> {
            if(c == finalCommas) {
                return PatternReplacementResult.REPLACE;
            }
            return PatternReplacementResult.CONTINUE;
        }));
        msg = msg.replaceText(b -> b.match(Pattern.compile("\\{COUNT}")).replacement(players.size()+""));


        String f = PlainTextComponentSerializer.plainText().serialize(msg);
        System.out.println(f);
        int c = 0;
        for(Character fChar : f.toCharArray()) {
            if(fChar == ',') c++;
        }
        if(c != 5) {
            throw new Exception("Wrong comma count! Had "+c+" when expected 5");
        }
    }
}
