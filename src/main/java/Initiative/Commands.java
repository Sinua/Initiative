package Initiative;

import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Commands {
    public static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("ping",
                event -> event.getMessage().getChannel().flatMap(channel -> channel.createMessage("Pong!"))
                        .then());

        commands.put("join", event -> Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                // join returns a VoiceConnection which would be required if we were
                // adding disconnection features, but for now we are just ignoring it.
                .flatMap(channel -> channel.join(spec -> spec.setProvider(PlayerHandler.getPlayerHandler(channel.getGuildId()).provider)))
                .doOnNext(voiceConnection -> Initiative.snowflakePlayer.get(event.getGuildId().get()).setVoiceConnection(voiceConnection))
                .then());

        commands.put("play", event -> Mono.justOrEmpty(event.getMember())
                .flatMap(Member::getVoiceState)
                .flatMap(VoiceState::getChannel)
                // join returns a VoiceConnection which would be required if we were
                // adding disconnection features, but for now we are just ignoring it.
                .flatMap(channel -> channel.join(spec -> spec.setProvider(PlayerHandler.getPlayerHandler(event.getGuildId().get()).provider)))
                .doOnNext(voiceConnection -> Initiative.snowflakePlayer.get(event.getGuildId().get()).setVoiceConnection(voiceConnection))
                .flatMap(voiceConnection -> Mono.justOrEmpty(event.getMessage().getContent()))
                .map(content -> new ArrayList(Arrays.asList(content.split(" "))))
                .doOnNext(command -> Initiative.playerManager.loadItem(PlayerHandler.getPlayerHandler(event.getGuildId().get()).parseSearchQuery(command), PlayerHandler.getPlayerHandler(event.getGuildId().get()).loadHandler))
                .flatMap(command -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(PlayerHandler.getPlayerHandler(event.getGuildId().get()).getNewTrackMessage())))
                .then());

        commands.put("pause", event -> Mono.justOrEmpty(event.getMessage().getContent())
                .doOnNext(command -> PlayerHandler.getPlayerHandler(event.getGuildId().get()).player.setPaused(true))
                .then());

        commands.put("resume", event -> Mono.justOrEmpty(event.getMessage().getContent())
                .doOnNext(command -> PlayerHandler.getPlayerHandler(event.getGuildId().get()).player.setPaused(true))
                .then());

        commands.put("seek", event -> Mono.justOrEmpty(event.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(message -> PlayerHandler.getPlayerHandler(event.getGuildId().get()).seekTrack(message.get(1)))
                .then());

        commands.put("np", event -> Mono.justOrEmpty(PlayerHandler.getPlayerHandler(event.getGuildId().get()).getNpMessage())
                .flatMap(message -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(message)))
                .then());

        commands.put("queue", event -> Mono.justOrEmpty(PlayerHandler.getPlayerHandler(event.getGuildId().get()).trackScheduler.getQueueMessage())
                .flatMap(message -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(message)))
                .then());

        commands.put("loop", event -> Mono.justOrEmpty(PlayerHandler.getPlayerHandler(event.getGuildId().get()))
                .doOnNext(playerHandler -> playerHandler.trackScheduler.toggleLoop())
                .then());

        commands.put("remove", event -> Mono.just(event.getMessage().getContent())
                .map(content -> Arrays.asList(content.split(" ")))
                .doOnNext(content -> PlayerHandler.getPlayerHandler(event.getGuildId().get()).trackScheduler.removeTrack(Integer.parseInt(content.get(1))))
                .then());

        commands.put("clear", event -> Mono.justOrEmpty(PlayerHandler.getPlayerHandler(event.getGuildId().get()).trackScheduler)
                .doOnNext(trackScheduler-> trackScheduler.clearQueue())
                .then());

        commands.put("dc", event -> Mono.justOrEmpty(PlayerHandler.getPlayerHandler(event.getGuildId().get()).getCurrentVoiceConnection())
                .flatMap(voiceConnection -> voiceConnection.disconnect())
                .then());

        commands.put("skip", event -> Mono.justOrEmpty(event.getGuildId().get())
                .flatMap(guildID -> event.getMessage().getChannel().flatMap(messageChannel -> messageChannel.createMessage(PlayerHandler.getPlayerHandler(guildID).getSkipMessage())))
                .doOnNext(guildId -> PlayerHandler.getPlayerHandler(event.getGuildId().get()).trackScheduler.nextTrack())
                .then());
    }
}
