/* Austin Smith
 * Project 5: TwitterBot Tweet Generator
 * 
 * MARKOV ORDER (line 30) = 2
 * 
 * Notes: - I commented out the google search text finder and also the novel that was attempting to be brought in.
 *     - I also commented out the information (tweets) being printed to the screen
 *     - The only thing that prints is the tweet and the amount of characters in the tweet
 *     - If the tweet is over 280 characters, it will not attempt to post the tweet and stop the program
 *     - The only thing i fixed up in the markovGen class is the generate function. This is so it checks for a period and if found, ends the generation of the tweet.
 *     - Added two functions to main, one fixes the words of beg/end symbols and the other pre-process the data
*/

//Imports
import processing.core.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import com.jaunt.JauntException;

//This class serves as a template for creating twitterbots and demonstrates string tokenizing and web scraping and the use of the 
//twitter API
public class TwitterBotMain extends PApplet {

	int markovOrder = 2; //markov chain is in order 3
	
	
	private ArrayList<String> tokens;
	private static String HEYER_TWITTER_URL = "https://twitter.com/GeorgetteRomanc"; //this is mine, you should use yours
	private static int TWITTER_CHAR_LIMIT = 140; //I understand this has changed... but forget limit
	
	//useful constant strings -- for instance if you want to make sure your tweet ends on a space or ending punctuation, etc.
	private static final String fPUNCTUATION = "\",.!?;:()/\\";
	private static final String fENDPUNCTUATION = ".!?;,";
	private static final String fREALENDPUNCTUATION = ".!?";

	private static final String fWHITESPACE = "\t\r\n ";
	
	//example twitter hastag search term
	private static final String fPASSIVEAGG = "funny";
	private static final String fCOMMA = ","; 
	private static final String fFUNNY = "funny";
	
	//object for markov chain
	MarkovGen<String> twitterGen = new MarkovGen<String>();
	ArrayList<String> dataNew = new ArrayList<String>();  //new data is the arraylist of words that pass the test of being included (meaning possibly used in generation)
	ArrayList<String> generatedTweet = new ArrayList<String>();  //tweet (before converted to string) as arrayList
	//word2 is a list of Special characters that should be deleted before/after words
	static Character[] word2 = {'@','#','!','?','"','+','-',',',' ','.','*'};
	static ArrayList<Character> noSymb = new ArrayList<Character>(Arrays.asList(word2));  //convert word2 into arrayList
	//handles twitter api
	TwitterInteraction tweet; 

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main("TwitterBotMain");  //Not really using processing functionality but ya know, you _could_. UI not required.
	}

	public void settings() {
		size(300, 300); //dummy window
	};

	public void setup() {
		String period = ".";
		tweet = new TwitterInteraction(); 
		
		//load novel functions (commented out)
		//loadNovel("data/The Grand Sophy excerpt.txt"); //TODO: must train from another source
		//println("Token size:"+tokens.size());
		
		
		//TODO: train an AI algorithm (eg, Markov Chain) and generate text for markov chain status
		
		//can train on twitter statuses -- note: in your code I would put this part in a separate function
		//but anyhow, here is an example of searrching twitter hashtag. You have to pay $$ to the man to get more results. :(
		//see TwitterInteraction class
		ArrayList<String> tweetResults = tweet.searchForTweets(fFUNNY);
		
		//print tweets (commented out)
/*		for (int i = 0; i < tweetResults.size(); i++) {
				println(i+ "-- "+tweetResults.get(i)); //just prints out the results for now
		}*/
		
		
		dataNew = preProcess(tweetResults); 			//get rid of words not wanted and find end of sentences
		twitterGen.train(dataNew);						//put data into markovGen
		twitterGen.calculateProb(markovOrder, true);		//add counts to transition table of words
		twitterGen.setData("Words", false);					//set data cnvers the counts in the transition table to come out with the probabilities of each occuring
		generatedTweet = twitterGen.generate(false, period);	//generate tweet
		
		//Make sure within Twitter limits (used to be 140 but now is more?)
		String status = String.join(" ", generatedTweet);
		int length;
		length = status.length();
		println("STATUS = " + length + " = "+ status);
		if(length < 280) tweet.updateTwitter(status);
		else println("Tweet was too long!");
		
				
		//prints the text content of the sites that come up with the google search of dogs
		//you may use this content to train your AI too
/*		Scraper scraper = new Scraper(); 
		try {
			scraper.scrapeGoogleResults("dogs");
			//scraper.scrape("http://google.com",  "dogs"); //see class documentation

		} catch (JauntException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		
	}

	//this loads the novel 'The Grand Sophy' given a path p -- but really will load any file.
/*	void loadNovel(String p) {
		String filePath = getPath(p);
		Path path = Paths.get(filePath);
		tokens = new ArrayList<String>();
		try {
			List<String> lines = Files.readAllLines(path);

			for (int i = 0; i < lines.size(); i++) {

				TextTokenizer tokenizer = new TextTokenizer(lines.get(i));
				Set<String> t = tokenizer.parseSearchText();
				tokens.addAll(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
			println("Oopsie! We had a problem reading a file!");
		}
	}
	*/
	
	void printTokens() {
		for (int i = 0; i < tokens.size(); i++)
			print(tokens.get(i) + " ");
	}

	//get the relative file path 
	String getPath(String path) {

		String filePath = "";
		try {
			filePath = URLDecoder.decode(getClass().getResource(path).getPath(), "UTF-8");
			filePath = filePath.substring(1, filePath.length()-1);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filePath;
	}

	
	public void draw() {
		// ellipse(width / 2, height / 2, second(), second());

	}
	
	
	public ArrayList<String> preProcess(ArrayList<String> data) {
		ArrayList<String> temp = new ArrayList<String>();
		String[] tokens = data.toArray(new String[0]);
		
		//extra variables commonly found within words that must be checked to ultimately delete word
		String checkHTTPS = "https";
		String checkSpace = " ";
		String checkPound = "#";
		String period = ".";
		String RT = "RT";

		for(int i=0; i<tokens.length;i++) {
			String[] sentenceHolder = tokens[i].split(" ");
			for(int x=0; x<sentenceHolder.length; x++) {
				
				boolean add = true;
				boolean endSentence = false;

				//checks each word if it is end of sentence and if so, add word (after being modifed) and also add a period to the list to ultimately assist in finding end of sentences 
				for(int f=0; f<fENDPUNCTUATION.length(); f++) {
					if(sentenceHolder[x].endsWith(fENDPUNCTUATION.substring(f,f+1)))  endSentence = true;
				}
				
				//if word starts with @, dont add
				if (sentenceHolder[x].startsWith("@")) add = false;
				//if word starts with https, dont add
				if (sentenceHolder[x].startsWith("https")) add = false;
				
				//if a word is to be added, this function modifies any symbols at the beggining or end of word.
				if(add) sentenceHolder[x] = deleteLastFirst(noSymb, sentenceHolder[x]);
				
				//after modifying word from symbols at end or front, chek to see if words contain the following: HTTPS, random space, #, and the symbol RT.
				//if any is found in the word, dont add the word
				if(sentenceHolder[x].toLowerCase().contains(checkHTTPS.toLowerCase())) add = false;
				if(sentenceHolder[x].toLowerCase().contains(checkSpace.toLowerCase())) add = false;
				if(sentenceHolder[x].toLowerCase().contains(checkPound.toLowerCase())) add = false;
				if(sentenceHolder[x].toLowerCase().contains(RT.toLowerCase())) add = false;
				//if null, dont add
				if(sentenceHolder[x] == null) add = false;
				
				//add word totemporary arrayList that will eventually be passed into the markov generator
				if(add) temp.add(sentenceHolder[x]);
				
				//if end of sentence, add period 
				if(endSentence) temp.add(period);
			}
		}
		return temp;
	}
	
	
	//delete symbols in front or back
	public static String deleteLastFirst(ArrayList<Character> list, String str) {
		//for loop goes through size of list of symbols not allowed
		for(int w=0; w<list.size(); w++) {
			//if the word isnt null and the last charctrer is one of the ymbols not allowed, delete the last symbol and check again
			if (str != null && str.length() > 0 && str.charAt(str.length() - 1) == list.get(w)) {
				str = str.substring(0, str.length() - 1);
				str = deleteLastFirst(list, str);
			}
			//if the word isnt null and the first charctrer is one of the ymbols not allowed, delete the first symbol and check again
			if (str != null && str.length() > 0 && str.charAt(str.length() - str.length()) == list.get(w)) {
				str = str.substring(1, str.length());
				str = deleteLastFirst(list, str);
			}
		}
		return str;
	}
}
	
