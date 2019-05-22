//MarkovGenOrderM

//imports 
import java.util.ArrayList;
import java.util.Random;

public class MarkovGen<E> {
	
	ArrayList<E> dataset;      //Holds data from main midi file
	ArrayList<E> symbFound;    //holds the symbols found 
	ArrayList<E> data;	       //holds the new song
	//transTable holds the transition table that is used to generate song
	ArrayList<ArrayList<Double>> transTable = new ArrayList<ArrayList<Double>>(); 
	//Keeps track of all the different orders found 
	ArrayList<ArrayList<E>> symbOrdersFound = new ArrayList<ArrayList<E>>();

	int orderOf = 1; 	//Used to scroll through order 1-10
	int numSymbols=0;
	boolean foundIt = false;	//Confirms that symbol orders is/isn't already in S
	int cI; 					//Current index
	int nI;						//next index
	int[] symbCount;			//number of different symbols found
	int[][] arr1;				//temporary array to hold count of symbols transition table
	double[][] tempArr;			//temp array to hold the actual probabilities 
	double random; 			    //holds random number
	double sum1; 				// sum 1 and 2 hold the values that determines where the random number falls in the transisiton table
	double sum2;
	Random rand = new Random();  //new random objects
	Random rand2 = new Random();

	int[][] transCount;  //this holds the transiiton table with the int counts before it is converted into a trans table of probabilities
	int lengthOfSong; //changes whether the generated song is the same size as the midi file or of size 20 for unit test 3
	
	//set data
	void train(ArrayList<E> tr) {
		dataset = tr;
	}
	
	//generate song
	public ArrayList<E> generate(boolean length, E per){			//E per now checks for period 
		data = new ArrayList<E>();  //holds data during generation 
		
		int randomOrderSelect = rand2.nextInt(numSymbols);   //rand number to randomly pick first order used for each song. This way, the beginning of the higher orders aren't the exact same
		data.addAll(symbOrdersFound.get(randomOrderSelect)); 			//add the first order
		int g = symbOrdersFound.indexOf(symbOrdersFound.get(randomOrderSelect));  //know what column the order is in so we can go to corresponding probabilities 
		if(length) lengthOfSong = dataset.size()-orderOf; 		//if unit test one, just make the songs the smae length as the midi file
		if(!length) lengthOfSong = 40;//if unit test three, make the songs of size 20
		
		boolean stopSentence = false;
		for(int p=0; p<lengthOfSong; p++) {  
			random = rand.nextDouble();   //get random number
			sum1 = 0;				  //checking if random number is between sum1 and sum2, if so this is where the next note is chosen
			sum2 = tempArr[g][0];   
			ArrayList<E> temp = new ArrayList<E>(); //temp array to hold symbol order 
			
			if(p>0) { //doesn't go through first iteration because first 
				int v = p;
				for(int o=0;o<orderOf;o++) {
					v++;
					temp.add(data.get(v-1));  //temp is now going to have the values of the most recent generation and only the amount of symbols needed in each order. This is how the next one will be chosen
				}
				g = symbOrdersFound.indexOf(temp);  //find index number of latest order
				if((g<0)||(g>=symbOrdersFound.size())) g=0;
				sum2 = tempArr[g][0]; //the bounds are moving to different numbers for the next number check (except initial)
			}
			
			for(int l=0; l<symbFound.size();l++) {
				if(random>=sum1 && random<sum2) { //check if random number is between bounds that would select symbol
					data.add(symbFound.get(l)); //add symbol to data and if so break out of for loop
					
					if(symbFound.get(l) == per) stopSentence = true;	//If a period is found, stop generation
				
					break;
				}
				sum1 = sum2; //move bounds for sum1
				if(l<symbFound.size()-1)sum2 = sum2 + tempArr[g][l+1]; //move bounds for sum2
				if(l==symbFound.size()-1)sum2 = sum2 + tempArr[g][symbFound.size()-1];
				//just some if statement check to check if all probabilities are 0 by chance and it fails
				if((sum2 == 0)&&(l == symbFound.size()-1)) g = g-2;
			}
			
			if(stopSentence) break;  //break out of loop and end function if end of sentence
		}
		//only clear if not Unit Test 3
		if(length) transTable.clear();
		if(length) symbOrdersFound.clear();
		return data;
	}

	
	//this function adds to the temporary array the number of times a symbol is found after the other
	//the numbers in each row are then divided by the symbol count to find the probabilities
	
	void calculateProb(int orderNum, boolean printData) {
		symbFound = new ArrayList<E>();  //array list for symbols found
		int countT = 1;
				for(int h=0; h<dataset.size()-1; h++) {		//This for loop simply gets the symbols found and also the count to be able to initialize some arrays
					E nextInd = dataset.get(h+1);
					E currInd = dataset.get(h);
					if(h==0) symbFound.add(currInd);
					foundIt = symbFound.contains(nextInd);
					if(!foundIt) {
						symbFound.add(nextInd);
						countT++;
					}	
				}
		ArrayList<E> currRow;
		ArrayList<E> check;  //temp arrayList to check equivalence between array lists
		orderOf = orderNum;  //markov Order of ...
		E indexPlus1 = null;  //this is the number that is directly after the order 
		int countP = 0;
		int indexOfOrder;
		int indexOf;  //find index of the var after the order that was recorded so we know where to add to the count in the transition table
		int holder = 0;
		transCount = new int[dataset.size()][countT];  //array of counts 
		boolean foundOrder = false; 
		
		for(int i=orderOf-1; i<dataset.size()-1; i++) {  //for loop to scroll through all of the data set
			currRow = new ArrayList<E>();
			
			for(int d=0;d<orderOf;d++) {   				//this gets the symbol orders
				indexOfOrder = d + countP;  			//when countP is added by 1 after, the next order will be different as it pushes it by 1. This will eventually go through the whole song
				currRow.add(dataset.get(indexOfOrder)); //currRow is a temporary array that will eventuall be added to a bigger transition table
				indexPlus1 = dataset.get(indexOfOrder+1);  //this gets us the index in symbolsFound of the symbol that falls after the order. This will then add a 1 
			}
			countP++;  //thi will move where the currRow collects data for the next loop.
			indexOf = symbFound.indexOf(indexPlus1);	//index of symbol 	
			for(int a = 0; a < symbOrdersFound.size(); a++) { //check if the order has already been found. 
				holder = a;
				check = new ArrayList<E>();
				check = symbOrdersFound.get(a);
				if(check.equals(currRow)) { //if found, do nothing and leave
					foundOrder=true;
					break;
				}else foundOrder = false;	
			}
			//adding a 1 in the transition tabke where data was present
			if(!foundOrder) {
				numSymbols++;
				symbOrdersFound.add(currRow); 
			}
			if((!foundOrder)&&(i > orderOf-1)) transCount[holder+1][indexOf] = transCount[holder+1][indexOf]+1;
			if((foundOrder)||(foundOrder == false && i == orderOf-1)) transCount[holder][indexOf] = transCount[holder][indexOf]+1;
		}//End of main for Loop
		tempArr = new double[symbOrdersFound.size()][symbFound.size()];
	}
	
	
	//this function sets the data from the temporary 2d array to multiple array lists to ultimately
	//be added to the transTable.
	void setData(String dataType, boolean printOffOn) {
		double m;   //m will eventually be probability in each trans table slot
		double converter; //for int in trans table slot,
		double converter2;  //for total ints in trans table row of slots
		int[] sumArr = new int [symbOrdersFound.size()]; //this array holds sum of every row in trans table
		int sum = 0;
		
		for(int w=0; w<symbOrdersFound.size();w++) {
			transTable.add(null);
		}
		
		for(int j=0;j<symbOrdersFound.size();j++) {
			for(int k=0;k<symbFound.size();k++) {
				sum = sum + transCount[j][k];  //get sum of row
			}
			sumArr[j] = sum; //store sum
			sum = 0;
		}
		for(int j=0;j<symbOrdersFound.size();j++) {  //nested loop to do math and ultimately set trassition table up with actual probabilities 
			ArrayList<Double> tempList = new ArrayList<Double>(); //create temporary array list to ultimately be added to trans table after values are divided by the sum
			for(int g=0;g<symbFound.size();g++) {  
				m = transCount[j][g];
				if(m==0) tempList.add(m);
				else  tempList.add(m/sumArr[j]);
				if(sumArr[j]==0) tempArr[j][g] = 0;
				else {
					converter = transCount[j][g];   //number count in each item
					converter2 = sumArr[j];			//divided by sum of row
					tempArr[j][g] = converter/converter2; 
				}	
			}
			transTable.set(j, tempList);
		}
		if(printOffOn) {
			//this.printData(dataType);
			//transTable.clear();
			//symbOrdersFound.clear();
		}
	}
	
	//This function prints the data. It is only called if the printData is true in main
	void printData(String info) {
		System.out.println("");
		System.out.println("Transition Table for "+info);
		System.out.println("");
		System.out.print("------------------------- ");
		for(int b=0;b<symbFound.size();b++) {
			System.out.print("["+symbFound.get(b)+"]  ");
		}
		System.out.println("");
		
		for(int j=0;j<symbOrdersFound.size();j++) {
			System.out.print(symbOrdersFound.get(j));
			for(int f=0;f<symbFound.size();f++) {
				System.out.print(" "+tempArr[j][f]);
				tempArr[j][f] = 0;
				transCount[j][f]=0;
			}
			System.out.println("");
		}
	}

}