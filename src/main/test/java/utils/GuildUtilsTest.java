package utils;

import com.bot.utils.GuildUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class GuildUtilsTest {

    @Test
    public void testGetHighestRole() {
        Role lowRole = mock(Role.class);
        Role midRole = mock(Role.class);
        Role highRole = mock(Role.class);
        Guild guild = mock(Guild.class);
        List<Role> roles = Arrays.asList(lowRole, midRole, highRole);

        doReturn(roles).when(guild).getRoles();
        doReturn(-1).when(lowRole).getPosition();
        doReturn(1).when(midRole).getPosition();
        doReturn(2).when(highRole).getPosition();

        Role returned = GuildUtils.getHighestRole(guild);
        assertEquals(highRole, returned);
    }
}
