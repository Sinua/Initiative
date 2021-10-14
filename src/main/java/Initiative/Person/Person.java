package Initiative.Person;

import Initiative.Initiative;
import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

public class Person {
    Snowflake userID;
    String displayName;
    List<Song> songs = new ArrayList<>();

    public Snowflake getUserID() {
        return userID;
    }

    public String getDisplayName() {
        return displayName;
    }

    private Person(Snowflake userID, String displayName) {
        this.userID = userID;
        this.displayName = displayName;
        Initiative.snowflakePersonMap.put(userID, this);
    }

    public Person getPerson(Snowflake userID, String displayName) {
        if(!Initiative.snowflakePersonMap.containsKey(userID))
            return new Person(userID, displayName);
        return Initiative.snowflakePersonMap.get(userID);
    }

    public void songPlayed(String songName) {
        Song newSong = new Song(songName);
        if(songs.contains(newSong)){
            songs.get(songs.indexOf(newSong)).addFreq();
        }
        else {
            songs.add(newSong);
        }
    }
}
