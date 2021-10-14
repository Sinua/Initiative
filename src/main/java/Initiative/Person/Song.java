package Initiative.Person;

public class Song {
    private final String name;
    private int freq = 0;

    public Song(String name) {
        this.name = name;
        freq = 1;
    }

    public void addFreq() {
        freq++;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Song){
            return ((Song) o).name == name;
        }
        return false;
    }
}
