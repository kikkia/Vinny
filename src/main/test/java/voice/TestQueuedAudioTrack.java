package voice;

import com.bot.voice.QueuedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestQueuedAudioTrack {

    private AudioTrack audioTrack;
    private AudioTrackInfo audioTrackInfo;

    @Before
    public void setUp() {
        audioTrack = mock(AudioTrack.class);
        audioTrackInfo = new AudioTrackInfo("testTitle",
                "testAuthor",
                1000,
                "testIdentifier",
                false,
                "testUri");
    }

    @Test
    public void testConstructor() {
        QueuedAudioTrack track = new QueuedAudioTrack(audioTrack, "name", 1234L);
        assertEquals(1234L, track.getRequesterID());
        assertEquals(audioTrack, track.getTrack());
    }

    @Test
    public void testToString() {
        when(audioTrack.getInfo()).thenReturn(audioTrackInfo);
        when(audioTrack.getDuration()).thenReturn(1000L);
        QueuedAudioTrack track = new QueuedAudioTrack(audioTrack, "name", 1234L);
        String expected = "[00:01] *testTitle* requested by name";

        assertEquals(expected, track.toString());
    }
}
