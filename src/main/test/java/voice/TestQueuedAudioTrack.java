package voice;

import com.bot.voice.QueuedAudioTrack;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestQueuedAudioTrack {

    private Track audioTrack;
    private TrackInfo audioTrackInfo;

    @Before
    public void setUp() {
        audioTrack = mock(Track.class);
        audioTrackInfo = new TrackInfo("id",
                true,
                "author",
                1000L,
                false,
                0,
                "title",
                "url",
                "source",
                "artwork",
                "src");
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
        QueuedAudioTrack track = new QueuedAudioTrack(audioTrack, "name", 1234L);
        String expected = "[00:01] *title* requested by name";

        assertEquals(expected, track.toString());
    }
}
