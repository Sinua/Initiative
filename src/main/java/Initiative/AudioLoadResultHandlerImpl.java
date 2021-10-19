package Initiative;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import discord4j.common.util.Snowflake;


public final class AudioLoadResultHandlerImpl implements AudioLoadResultHandler {

    private final AudioPlayer player;
    private final Snowflake guildID;

    public AudioLoadResultHandlerImpl(final AudioPlayer player, final Snowflake guildID) {
        this.player = player;
        this.guildID = guildID;
    }

    @Override
    public void trackLoaded(final AudioTrack track) {
        Initiative.snowflakePlayer.get(guildID).trackScheduler.queue(track, player);
    }

    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        for(AudioTrack at : playlist.getTracks())
            Initiative.snowflakePlayer.get(guildID).trackScheduler.queue(at, player);
    }

    @Override
    public void noMatches() {
        // LavaPlayer did not find any audio to extract
    }

    @Override
    public void loadFailed(final FriendlyException exception) {
        System.out.println("loadFailed :" + exception.getMessage());
    }

}
