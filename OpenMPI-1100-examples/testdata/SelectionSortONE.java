/*
 * Copyright (c) 2011      Cisco Systems, Inc.  All rights reserved.
 *
 * Simple ring test program
 */

import mpi.* ;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList; 
 
class SelectionSortONE {
/* This method is from  http://java2novice.com/java-sorting-algorithms/selection-sort/ */
    public static void selectionSort( int [ ] a ){
	
	     for (int i = 0; i < a.length - 1; i++){
	         
	             int index = i;
	             for (int j = i + 1; j < a.length; j++){
	                 if (a[j] < a[index]){
	                     index = j;}
	             }    
	             int smallerNumber = a[index]; 
	             a[index] = a[i];
	             a[i] = smallerNumber;
	         }
	} 	

    public static void selectionSort( int [ ] a , int length )
	{
	     for (int i = 0; i <length - 1; i++){
	         
	             int index = i;
	             for (int j = i + 1; j < length; j++){
	                 if (a[j] < a[index]){
	                     index = j;}
	             }    
	             int smallerNumber = a[index]; 
	             a[index] = a[i];
	             a[i] = smallerNumber;
	         }
	} 		

  private static void writeTextFile(File file, String text) {
	// physically write file
	try {
		FileOutputStream fo = new FileOutputStream(file);
		fo.write(text.getBytes());
		fo.close();
	} catch (IOException e) {
	}
    }

    static public void main(String[] args) throws MPIException {
	
	MPI.Init(args) ;
	long startTime = 0 ; 
	int source;  // Rank of sender
	int dest;    // Rank of receiver 
	int tag=50;  // Tag for messages	
	int next;
	int prev;
	int message[]	 = {2, 3, 4, 12, 11, 1, 99,87,98,99, 999,165,433,423, 989, 423, 533, 660, 604, 776,999,165,433,423, 989, 423, 533, 660, 604, 776,1912, 1413, 
                    1104, 1212, 1311, 1231, 1199,1487,1098,1099,3999,2165,1433,1423, 3989, 7423, 3533, 1660, 1604, 2776,10};
	int[] sizeOfArray = 
      {1000,10000,1000,10000,100000,1000,10000,100000,1000,10000,100000,1000,10000,100000,1000,10000,100000,1000,10000,100000,1000,10000,100000};  
//	int ia = 0 ; 	
	int ia = Integer.parseInt(args[0] ) ; 
	int count = 0 ; 
	int filesize = sizeOfArray[ia] ; 
	int [] eachfile = new int[filesize];
	int myrank = MPI.COMM_WORLD.getRank() ;
	int size = MPI.COMM_WORLD.getSize() ;
	int c = 0 , c2 = 0 , c3 =0 , c4= 0 ; // count 
	ArrayList <int[]> list = new ArrayList<int[]>() ;
	for ( int i = 0 ; i < size ; i++ ){
	  list.add(new int[filesize] ) ; 
	}

	/* Calculate the rank of the next process in the ring.  Use the
	   modulus operator so that the last process "wraps around" to
	   rank zero. */
	next = (myrank + 1) % size;
	prev = (myrank + size - 1) % size;
	
	/*name of all the data file so it easy when execute just to write the index as arguments*/
    	String[] filenames = {"med.3.killer.1000.txt","med.3.killer.10000.txt","rand.dups.1000.txt","rand.dups.10000.txt","rand.dups.100000.txt",
    			"rand.no.dups.1000.txt","rand.no.dups.10000.txt","rand.no.dups.100000.txt", "rand.steps.1000.txt","rand.steps.10000.txt",
    			"rand.steps.100000.txt", "rev.partial.1000.txt","rev.partial.10000.txt","rev.partial.100000.txt", "rev.saw.1000.txt","rev.saw.10000.txt",
    			"rev.saw.100000.txt", "seq.partial.1000.txt","seq.partial.10000.txt","seq.partial.100000.txt","seq.saw.1000.txt",
    			"seq.saw.10000.txt", "seq.saw.100000.txt"};
    	File savedfile = new File(size+"SEresult/" + filenames[ia] + size+"SELECTIONSORTED.txt"); 		
    	
        /* If we are the "master" process (i.e., MPI_COMM_WORLD rank 0),
	put the number of times to go around the ring in the
	message. */
        if (0 == myrank) {
        /* Process 0 will read the file and put it in an array */	
            try {
    		File file = new File(filenames[ia]);
    	
    		startTime =  System.currentTimeMillis();
             	BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
             	StringBuffer tmp = new StringBuffer();
             	while (input.ready()) {   
             	    String line = input.readLine() ;
                    eachfile[count] = Integer.parseInt(line);
                    count++; 
             	}
     	    } catch (Exception e) {        }	
	    message[0] = 1 ; 
	    System.out.println( myrank +" COUNT : "+ count );
	    System.out.println("Process 0 sending " + message + " to rank " + next + " (" + size + " processes in ring) -"+ tag); 
	    /*Send the nubmer of time that process need to go around the ring*/
	    MPI.COMM_WORLD.send(message, 1,  MPI.INT, next, tag);	
	    /*Send the data array that read from the file */
	    MPI.COMM_WORLD.send(eachfile, filesize,  MPI.INT, next, tag);
	    /*Send number (4 if there 4 process , 6 if there 6 process) of array  */
	    for ( int i = 0 ; i < list.size() ; i++ ) {
	      MPI.COMM_WORLD.send(list.get(i), filesize,  MPI.INT, next, tag);
	    }
	
	   
	}
	/* Pass the message around the ring.  The exit mechanism works as
	   follows: the message (a positive integer) is passed around the
	   ring.  Each time it passes rank 0, it is decremented.  When
	   each processes receives a message containing a 0 value, it
	   passes the message on to the next process and then quits.  By
	   passing the 0 message first, every process gets the 0 message
	   and can quit normally. */
	
	while (true) {
		
	    /* Recv all the array that sent*/	
	    MPI.COMM_WORLD.recv(message,1, MPI.INT, prev, tag);
	    MPI.COMM_WORLD.recv(eachfile, filesize,  MPI.INT, prev, tag);
	    for ( int i = 0 ; i < list.size() ; i++ ) {
	      MPI.COMM_WORLD.recv(list.get(i), filesize,  MPI.INT, prev, tag);
	    }

	    System.out.println( myrank +" COUNT : "+ count );	
	 if (0 == myrank) {
		--message[0];
		System.out.println ( "Here" + myrank);
		/* Calculate the range that process 0 should take from the file */
		float bound =  ((float)filesize/ (float)size);
		float lb = (float) bound* (float)myrank ; 
		float ub = (float)(bound * ((float)myrank+1))-1;
		int lowerBound = Math.round(lb);
		int upperBound = Math.round(ub);

		System.out.println("Process 0 decremented value: " + message[40] + " -"+ tag);
		int[] gather = new int[filesize] ; 
		c = 0 ;
		/* Go through the data array and pick the data that fit the range */
		for ( int i = 0 ; i < filesize ;i++ ){
			if(eachfile[i] >= lowerBound && eachfile[i] <=  upperBound ){
				gather[c] = eachfile[i] ; 
				c++ ; 
			}
		}
		/* Sort the process 's arry */
		SelectionSortONE.selectionSort (gather, c ) ;
		int count1 = 0 ; 
		/* Copy it to the list.get(0) array */
		for ( int i = 0 ; i< filesize ; i++ ){
		   list.get(0)[i] = gather[count1] ; 
		   //System.out.println( myrank + " * "+ gather[count1] +" * "+ count ) ; 
		   count1++ ; 
		   if(count1 == c ){ break ; }
		}
	
		
		
 	 }	
		
	else {	
		System.out.println ( "Here" + myrank);
		/* Calculate the range that process 0 should take from the file */
		float bound =  ((float)filesize/ (float)size);
		float lb = (float) bound* (float)myrank ; 
		float ub = (float)(bound * ((float)myrank+1))-1;
		int lowerBound = Math.round(lb);
		int upperBound = Math.round(ub);
		if ( myrank == size-1 ) {
		  upperBound = Math.round((bound * (myrank+1))); 
		}
		else {
		  upperBound = Math.round((bound * (myrank+1)) -1  );  // ( 0.25*1)
		}  

		System.out.println("Process 0 decremented value: " + message[0] + " -"+ tag);
		int[] gather = new int[filesize] ; 
		c = 0 ;
		/* Go through the data array and pick the data that fit the range */
		for ( int i = 0 ; i < filesize ;i++ ){
		  if(eachfile[i] >= lowerBound && eachfile[i] <=  upperBound ){
			gather[c] = eachfile[i] ; 
			c++ ; 
		  }
		}
		SelectionSortONE.selectionSort(gather, c ) ;
		
		int count1 = 0 ; 
		int []array = list.get(myrank) ; 
		for ( int i = 0 ; i< filesize ; i++ ){
		  array[i] = gather[count1] ; 
		  //System.out.println( myrank + " * "+ gather[count1] +" * "+ count ) ; 
		  count1++ ; 
		  if(count1 == c ){ break ; }
		   
		}
	
		
	}
   
	  MPI.COMM_WORLD.send(message, 1, MPI.INT, next, tag);
	  MPI.COMM_WORLD.send(eachfile, filesize,  MPI.INT, next, tag);
	  for ( int i = 0 ; i < list.size() ; i++ ) {
	      MPI.COMM_WORLD.send(list.get(i), filesize,  MPI.INT, next, tag);
	  }

	  
	  if (0 == message[0]) {
		System.out.println("Process " + myrank + " exiting");
        	break;
	   }
	}

	/* The last process does one extra send to process 0, which needs
	   to be received before the program can exit */
  
	if (0 == myrank) {
	    MPI.COMM_WORLD.recv(message,1, MPI.INT, prev, tag);
	    MPI.COMM_WORLD.recv(eachfile, filesize,  MPI.INT, prev, tag);
	    for ( int i = 0 ; i < list.size() ; i++ ) {
	      MPI.COMM_WORLD.recv(list.get(i), filesize,  MPI.INT, prev, tag);
	    }
	    
	
	    if (list.get(0)[0] != 0){
	      for( int a = 1 ; a < list.size() ; a++){ // copy everything into first array 
		int index = 0 ; 
		for ( int i = 0 ; i< filesize ; i++ ){  
		    if ( list.get(0)[i] == 0 && list.get(a)[index] != 0 ) {
		      list.get(0)[i] = list.get(a)[index] ; 
		      index ++ ; 
		    }
		    if(list.get(a)[index] == 0 ) { break ; }
		}
	      } 
	   }
	   
	   
	   String text = "" ;
	   for ( int i = 0 ; i< filesize ; i++ ){
		  //System.out.println ( "Finally : " + list.get(0)[i] + " - index *" + i) ;
		  text+= list.get(0)[i] + "\n" ;
	   }
	   long stopTime =   System.currentTimeMillis(); 
	   long time =stopTime - startTime ; 
	   String t = "Time took to sort : "+ time + " ms \n";
	   writeTextFile((savedfile), t+""+text ) ;
	  
	  
	   System.out.println ( "Sorted array save to " + savedfile.getName() );
	   System.out.println ("Time it take to sort "+  filenames[ia] +" : "+time + " ms"); 
		
	}
    
	MPI.Finalize();
    }
}
