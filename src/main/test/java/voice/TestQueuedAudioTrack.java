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
    public void setup() {
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
        QueuedAudioTrack track = new QueuedAudioTrack(audioTrack, 1234L);
        assertEquals(1234L, track.getRequesterID());
        assertEquals(audioTrack, track.getTrack());
    }

    @Test
    public void testToString() {
        when(audioTrack.getInfo()).thenReturn(audioTrackInfo);
        when(audioTrack.getDuration()).thenReturn(1000L);
        QueuedAudioTrack track = new QueuedAudioTrack(audioTrack, 1234L);
        String expected = "[00:01] *testTitle* requested by <@1234>";

        assertEquals(expected, track.toString());
    }

    @Test
    public void testMsToMinSec() {
        String expected = "10:10"; // 10 mins 10 seconds, 610000 - 610999ms
        assertEquals(expected, QueuedAudioTrack.msToMinSec(610000));
        assertEquals(expected, QueuedAudioTrack.msToMinSec(610999));

        expected = "01:14:54"; // 1hr 14mins 54 seconds = 4494000ms - 4494999ms
        assertEquals(expected, QueuedAudioTrack.msToMinSec(4494000));
        assertEquals(expected, QueuedAudioTrack.msToMinSec(4494999));
    }
}
