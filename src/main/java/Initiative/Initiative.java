package Initiative;

import Initiative.Person.Person;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

public class Initiative {

    public static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    public static Map<Snowflake, PlayerHandler> snowflakePlayer = new HashMap<>();
    public static Map<Snowflake, Person> snowflakePersonMap = new HashMap<>();

    public static void main(final String[] args) {
    try {
        final GatewayDiscordClient client = DiscordClientBuilder.create(System.getenv("DISCORD_KEY")).build().login().block();
        AudioSourceManagers.registerRemoteSources(Initiative.playerManager);
        Initiative.playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);

        Commands commands = new Commands();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                        .flatMap(content -> Flux.fromIterable(Commands.commands.entrySet())
                                .filter(entry -> content.startsWith(Constants.COMMAND_PREFIX + entry.getKey()))
                                .flatMap(entry -> entry.getValue().execute(event)).next())).subscribe();




        client.onDisconnect().block();
    } finally {

    }

    }
}


