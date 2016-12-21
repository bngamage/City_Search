# City_Search
This is an error-tolerant prefix search using a q-gram index and prefix edit distance. 
Here we have used cities2.txt which includes city names and their locations.
When a user types a city name it will display proper city names beneath the search field which is similar to the google search.
And when user select a city and then click go, it will display the city in google maps, in a new tab.

To run the project, please download the code and navigate to the folder in terminal and then run the following commands.

1) javac SearchServerMain.java

2) jar cmvf META-INF/MANIFEST.MF SearchServerMain.jar *

3) make PORT=3311 FILE=cities2.txt start


(Reuirements for the project: Java and make should be installed in the host machine)

After running above three commands open "localhost:3311/search.html" in a web browser.
