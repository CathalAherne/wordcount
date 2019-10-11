import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
   Class which returns the word count of the files located in directory specified
 */
public class Wordcount {

    private Map<String, Integer> result;
    private List<File> files;
    private FileTime dateTime;
    private File rootFolder;

    public Wordcount(String fileLocation, String dateTime){
        result = new HashMap<>();
        this.dateTime = convertStringToDateTime(dateTime);
        rootFolder = new File(fileLocation);
    }

    /*
        This method will get the files which were created/ modified at and before the specified time accurate to the second
        and persist all the count of all occurrences
     */
    public void getCount(){

        result = new HashMap<>();

        if (rootFolder.exists()) {

            getFilesForCount();
            System.out.println(files.size());

            for (File file : files) {
                Scanner input = null;
                try {
                    input = new Scanner(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                input.useDelimiter(" +");
                while (input.hasNext()) {
                    String word = input.next().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                    result.merge(word, 1, (a, b) -> a + b);
                }
            }
        }
    }

    private FileTime convertStringToDateTime(String dateAsString){
        long milis = 0;
        try {
            milis = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse(dateAsString)
                    .getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return FileTime.fromMillis(milis);
    }

    private void getFilesForCount(){
        List<Path> filteredPaths = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(rootFolder.getAbsolutePath()))) {
            filteredPaths = paths.filter(Files::isRegularFile).filter(file -> {
                try {
                    return Files.readAttributes(file, BasicFileAttributes.class).creationTime().compareTo(this.dateTime) <= 0;
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        files = filteredPaths.stream().map(path -> path.toFile()).collect(Collectors.toList());
    }

    /*
        Print results of Word Count
     */
    public void printResults(){
        result.entrySet().stream().forEach(e-> System.out.println(e.getKey() + " : " + e.getValue()));
    }


    public static void main(String[] args){
        Wordcount wordcount = new Wordcount("C:\\usefulInformation\\words", "11.10.2019 09:03:00");
        wordcount.getCount();
        wordcount.printResults();

    }

}
