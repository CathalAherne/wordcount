# wordcount

<html>
<p> Wordcount application, which takes a directory location and a date time in the format dd.MM.yyyy hh:mm:ss
     and returns the word count of all files within this directory which have been created / editied before or on this date       time
     
    Note : If Date time is not specified, current date time will be set as default
           All non alphanumberic characters are filtered out and words are transformed to lowercase
     
To Run :
    
       javac Wordcount.java
       java Wordcount "/Users/Cathal/Desktop/cantrbry" "01.01.01 00:00:00"
       or to use current date time
       java Wordcount "/Users/Cathal/Desktop/cantrbry"
     

