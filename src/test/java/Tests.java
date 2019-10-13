import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;
import static junit.framework.TestCase.assertTrue;

public class Tests {

    @Test
    public void testWithNoDirectorySpecified(){
        String[] args = {getCurrentTime()};
        WordCount.main(args);
    }

    @Test
    public void testWithNoTime(){
        String[] args = {getCurrentTime()};
        WordCount.main(args);
    }

    @Test
    public void testWithSmallFilesEvenNumber(){
        WordCount wordCount = new WordCount("fourSmallFiles", getCurrentTime());
        wordCount.getCount();
        Boolean allMatch = wordCount.getResultAsMap().entrySet().stream()
                .allMatch(e -> e.getValue().equals(getFourSmallFilesWordCountTest().get(e.getKey())));

        assertTrue(allMatch);
    }

    @Test
    public void testWithFilesOddNumber(){
        WordCount wordCount = new WordCount("fiveSmallFiles", getCurrentTime());
        wordCount.getCount();
        Boolean allMatch = wordCount.getResultAsMap().entrySet().stream()
                .allMatch(e -> e.getValue().equals(getFiveSmallFilesWordCountTest().get(e.getKey())));

        assertTrue(allMatch);
    }

    /*
        The subset of word count values tested against here were ascertained using grep -roh Word . | wc -w
        TODO : This test currently fails, more than likely something to do with the way words are separated using delimiters
               with the Grep approach, or how special characters are handled.
     */

    @Test
    public void testWithBiggerFiles(){
        WordCount wordCount = new WordCount("3BiggerFiles", getCurrentTime());
        wordCount.getCount();
        Iterator iter = getThreeBiggerFilesWordCountSubset().entrySet().iterator();
        Boolean allMatch = true;
        while (iter.hasNext()){
            Map.Entry entry = (Map.Entry) iter.next();
            if (entry.getValue() != wordCount.getResultAsMap().get(entry.getKey())){
                allMatch = false;
                break;
            }
        }

        assertTrue(allMatch);
    }



    private Map getFourSmallFilesWordCountTest(){
        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("Hello", 4);
        expectedResult.put("How", 4);
        expectedResult.put("are", 4);
        expectedResult.put("you", 4);
        return expectedResult;
    }

    private Map getFiveSmallFilesWordCountTest(){
        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("Hello", 5);
        expectedResult.put("How", 5);
        expectedResult.put("are", 5);
        expectedResult.put("you", 5);
        return expectedResult;
    }


    private Map getThreeBiggerFilesWordCountSubset(){
        Map<String, Integer> expectedResult = new HashMap<>();
        expectedResult.put("Alice", 790);
        expectedResult.put("home", 17);
        expectedResult.put("rabbit", 90);
        expectedResult.put("key", 23);
        return expectedResult;
    }

    private String getCurrentTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
