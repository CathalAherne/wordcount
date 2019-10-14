

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/*
   Class which returns the word count of the files located in directory specified
*/
public class WordCount {

    private List<File> files;
    private FileTime dateTime;
    private File rootFolder;
    private List<Map<String, Integer>> maps;

    public WordCount(String fileLocation, String dateTime) {
        this.dateTime = convertStringToDateTime(dateTime);
        rootFolder = new File(fileLocation);
    }

    /*
        This method will get the files which were created / modified at and before the specified time accurate to the second
        and store the count of all occurrences
     */
    public void getCount() {
        maps = new ArrayList<>();

        if (rootFolder.exists()) {

            getFilesForCount();
            ExecutorService es = Executors.newCachedThreadPool();

            for (int i = 0; i < files.size(); i++) {
                int finalI = i;
                es.execute(new Runnable() {
                    @Override
                    public void run() {
                        maps.add(new HashMap<>());
                        Scanner input = null;
                        try {
                            input = new Scanner(files.get(finalI));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        input.useDelimiter("\\s");
                        while (input.hasNext()) {
                            String word = input.next().replaceAll("[^\\p{L}\\p{Z}]", "");
                            maps.get(finalI).merge(word, 1, (a, b) -> a + b);
                        }
                        input.close();
                    }
                });
            }

            es.shutdown();
            boolean finished = false;
            try {
                finished = es.awaitTermination(3, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (finished && maps.size() > 1) {
                mergeMaps();
            }
        }
    }

    private void mergeMaps() {

        if (maps.size() % 2 != 0) {
            mergeTwoMaps(maps.get(0), maps.get(maps.size() - 1));
            maps.remove(maps.get(maps.size() - 1));
        }

        while (maps.size() > 1) {
            ExecutorService es = Executors.newCachedThreadPool();
            for (int i = 0; i < maps.size() - 1; i++) {
                if (i % 2 == 0) {
                    int finalI = i;
                    es.execute(new Runnable() {
                        @Override
                        public void run() {
                            mergeTwoMaps(maps.get(finalI), maps.get(finalI + 1));
                        }
                    });
                }

            }
            es.shutdown();
            boolean finished = false;
            try {
                finished = es.awaitTermination(3, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (finished && maps.size() > 1) {
                removeUnevenMaps(maps);

            }
        }
    }

    private static void mergeTwoMaps(Map<String, Integer> map1, Map<String, Integer> map2) {

        Iterator iter = map2.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> pair = (Map.Entry) iter.next();
            if (map1.containsKey(pair.getKey())) {
                int count = map1.get(pair.getKey()) + pair.getValue();
                map1.put(pair.getKey(), count);
            } else {
                map1.put(pair.getKey(), 1);
            }
        }
    }

    private static void removeUnevenMaps(List<Map<String, Integer>> maps) {
        List<Integer> toRemove = new ArrayList<>();
        for (int i = 0; i < maps.size(); i++) {
            if (i % 2 != 0) {
                toRemove.add(i);
            }
        }
        int removed = 0;
        for (Integer indexToRemove : toRemove) {
            Map mapToRemove = maps.get(indexToRemove - removed);
            maps.remove(mapToRemove);
            removed += 1;
        }
    }

    private static FileTime convertStringToDateTime(String dateAsString) {
        long milis = 0;
        try {
            milis = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss").parse(dateAsString)
                    .getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return FileTime.fromMillis(milis);
    }

    private void getFilesForCount() {
        List<Path> filteredPaths = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(rootFolder.getAbsolutePath()))) {
            // Filter out files created after date and also hidden files
            filteredPaths = paths.filter(Files::isRegularFile).filter(file -> !file.toFile().isHidden()).filter(file -> {
                try {
                    return Files.readAttributes(file, BasicFileAttributes.class).creationTime().compareTo(this.dateTime) >= 0;
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
    public void printResults() {
        maps.get(0).entrySet().stream().forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
    }

    public Map<String, Integer> getResultAsMap() {
        return maps.get(0);
    }

    public static void main(String[] args) {
        File directory;

        if (args.length != 0) {
            directory = new File(args[0]);
        } else {
            System.out.println("Directory can not be empty");
            return;
        }

        if (!directory.exists()) {
            System.out.println("Directory is not valid");
            return;
        }

        // Set default time to current time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String dateAsString = args.length > 1 ? args[1] : formatter.format(date);

        final long startTime = System.currentTimeMillis();
        WordCount wordCount = new WordCount(args[0], dateAsString);
        wordCount.getCount();
        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime));
        wordCount.printResults();
    }
}
