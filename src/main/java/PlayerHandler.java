import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
import com.google.api.services.youtube.model.SearchListResponse;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import discord4j.common.util.Snowflake;
import discord4j.voice.AudioProvider;
import discord4j.voice.VoiceConnection;
import com.google.api.services.youtube.YouTube;

import java.time.Instant;
import java.util.List;

public class PlayerHandler {

    public final AudioPlayer player;
    public AudioProvider provider;
    public final AudioLoadResultHandler loadHandler;
    public final TrackScheduler trackScheduler;
    public final Snowflake guildId;
    private VoiceConnection currentVoiceConnection;
    private YouTube youtubeService;

    public PlayerHandler(Snowflake guildId) {
        this.guildId = guildId;
        Initiative.snowflakePlayer.put(guildId, this);
        player = Initiative.playerManager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
        provider = new LavaPlayerAudioProvider(player);
        loadHandler = new AudioLoadResultHandlerImpl(player,guildId);
        try{
            youtubeService = YoutubeSearcher.getService();
        } catch (Exception e) {
            System.out.printf(e.getMessage());
        }
    }

    public static PlayerHandler getPlayerHandler(Snowflake guildId) {
        if(!Initiative.snowflakePlayer.containsKey(guildId))
            return new PlayerHandler(guildId);
        return Initiative.snowflakePlayer.get(guildId);
    }

    public void setVoiceConnection(VoiceConnection vc) {
        this.currentVoiceConnection = vc;
    }

    public VoiceConnection getCurrentVoiceConnection() {
        return currentVoiceConnection;
    }

    public void seekTrack(String pos) {
        long positionInMilli = Long.parseLong(pos) * 1000;
        player.getPlayingTrack().setPosition(positionInMilli);
    }

    public long getTrackPos() {
        return (player.getPlayingTrack().getPosition())/1000;
    }

    private String formatPosition(long pos) {
        String hr = (pos/3600>0) ? pos/3600 + "h:" : "";
        String min = (pos/60>0) ? pos/60 + "m:" : "";
        String sec = pos%60 + "s";

        return hr + min + sec;
    }

    public String getNpMessage() {
        long startTime = Instant.now().getEpochSecond();
        while(player.getPlayingTrack() == null && Instant.now().getEpochSecond() - startTime <= 1){
            try {
                Thread.sleep(500);
            }catch (Exception e) {
                System.out.println(e);
            }
        }
        if (player.getPlayingTrack() == null){
            return Constants.CODE_BLOCK_THING + "NO TRACK IS PLAYING !!!!" + Constants.CODE_BLOCK_THING;
        }
        return Constants.CODE_BLOCK_THING + formatPosition(getTrackPos()) + " / " + formatPosition(player.getPlayingTrack().getInfo().length/1000)
                + " - " + player.getPlayingTrack().getInfo().title + Constants.CODE_BLOCK_THING;
    }

    private String makeQueryYTLink(List<String> query){
        query.remove(0);
        String searchQ = String.join(" ", query);
        try {
            YouTube.Search.List request = youtubeService.search().list("id,snippet");
            request.setKey(Constants.apiKey);
            request.setQ(searchQ);
            request.setType("video");
            request.setFields("items(id/videoId)");
            SearchListResponse response = request.execute();
            if(response != null) {
                String vidID = Constants.YOUTUBE_VIDEO_PREIX + response.getItems().iterator().next().getId().getVideoId();
                System.out.println(vidID);
                return vidID;
            }
        } catch (Exception e){
            System.out.printf(String.valueOf(e));
        }
        return searchQ;
    }

    public String parseSearchQuery(List<String> query){
        if(query.size() == 1)
            return "XD";
        if(query.get(1).contains("youtube.com")) {
            return query.get(1);
        }
        String vidID = makeQueryYTLink(query);
        return vidID;
    }
}
