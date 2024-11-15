package Initiative;


import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final BlockingQueue<AudioTrack> queue;
    private final AudioPlayer player;
    private AudioTrack mostRecentTrack = null;
    private boolean shouldLoop = false;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }


    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track, AudioPlayer player) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        mostRecentTrack = track;
        if (!player.startTrack(track, true)) {
            queue.offer(track);
            System.out.println(getMostRecentTrack().getInfo().title + " XD");
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        AudioTrack nextTrack = queue.poll();
        if (shouldLoop){
            queue.offer(nextTrack.makeClone());
        }
        player.startTrack(nextTrack, false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }

    public List<AudioTrack> getQueue() {
        List<AudioTrack> audioTracks = new ArrayList<>();
        for (AudioTrack audioTrack:queue)
            audioTracks.add(audioTrack);
        return audioTracks;
    }

    public AudioTrack getMostRecentTrack() {
        return mostRecentTrack;
    }

    public void clearMostRecentTrack() {
        mostRecentTrack = null;
    }

    public String getQueueMessage() {
        int c = 0;
        List<AudioTrack> audioTracks = getQueue();
        String message = Constants.CODE_BLOCK_THING + Constants.NEWLINE;
        if(audioTracks.isEmpty())
            return message + "Queue is empty!!!!!!!!!" + Constants.CODE_BLOCK_THING;
        for (AudioTrack at : audioTracks)
            message += c++ +". " + at.getInfo().title + "\n";
        message+= Constants.CODE_BLOCK_THING;
        return message;

    }

    public void removeTrack(int index) {
        if(!queue.isEmpty() && queue.size() > index) {
            List<AudioTrack> audioTracks = getQueue();
            queue.remove(audioTracks.get(index));
        }
    }

    public void clearQueue() {
        while(!queue.isEmpty())
            queue.remove();
    }

    public void toggleLoop() {
        try{
            if (player.getPlayingTrack() != null && shouldLoop == false)
                queue.offer(player.getPlayingTrack().makeClone());
            shouldLoop = shouldLoop ^ true;
        } catch(Exception e){
            System.out.println(e);
        }

    }
}
