package voice;

import com.bot.Bot;
import com.bot.voice.VoiceSendHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class TestVoiceSendHandler {

    private AudioPlayer audioPlayer;
    private Bot bot;

    @Before
    public void setup() {
        audioPlayer = mock(AudioPlayer.class);
        bot = mock(Bot.class);
    }

    @Test
    public void testConstructor() {
        VoiceSendHandler voiceSendHandler = new VoiceSendHandler(1234, audioPlayer, bot);

        assertEquals(audioPlayer, voiceSendHandler.getPlayer());
        assertNull(voiceSendHandler.getNowPlaying());
        assertEquals(0, voiceSendHandler.getTracks().size());
    }
}
