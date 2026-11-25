package utils;

import com.bot.utils.ScheduledCommandUtils;
import net.dv8tion.jda.api.entities.Webhook;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScheduledCommandUtilsTest {

    @Test
    public void testFindReusableVinnyWebhookSkipsTokenlessWebhooks() {
        Webhook tokenlessVinny = mock(Webhook.class);
        when(tokenlessVinny.getName()).thenReturn("vinny");
        when(tokenlessVinny.getToken()).thenReturn(null);

        Webhook emptyTokenVinny = mock(Webhook.class);
        when(emptyTokenVinny.getName()).thenReturn("Vinny");
        when(emptyTokenVinny.getToken()).thenReturn("");

        Webhook otherHook = mock(Webhook.class);
        when(otherHook.getName()).thenReturn("other");
        when(otherHook.getToken()).thenReturn("other-token");

        Webhook validVinny = mock(Webhook.class);
        when(validVinny.getName()).thenReturn("Vinny");
        when(validVinny.getToken()).thenReturn("secret-token");

        assertFalse(ScheduledCommandUtils.findReusableVinnyWebhook(
                Arrays.asList(tokenlessVinny, emptyTokenVinny, otherHook)).isPresent());

        Optional<Webhook> found = ScheduledCommandUtils.findReusableVinnyWebhook(
                Arrays.asList(tokenlessVinny, otherHook, validVinny));
        assertTrue(found.isPresent());
        assertEquals(validVinny, found.get());

        assertFalse(ScheduledCommandUtils.findReusableVinnyWebhook(Collections.emptyList()).isPresent());
    }
}
