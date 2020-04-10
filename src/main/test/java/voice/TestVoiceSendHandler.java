package voice;

import com.bot.voice.VoiceSendHandler;
import lavalink.client.player.LavalinkPlayer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class TestVoiceSendHandler {

    private LavalinkPlayer audioPlayer;

    @Before
    public void setUp() {
        audioPlayer = mock(LavalinkPlayer.class);
    }

    @Test
    public void testConstructor() {
        VoiceSendHandler voiceSendHandler = new VoiceSendHandler(audioPlayer);

        assertEquals(audioPlayer, voiceSendHandler.getPlayer());
        assertNull(voiceSendHandler.getNowPlaying());
        assertEquals(0, voiceSendHandler.getTracks().size());
    }
}
