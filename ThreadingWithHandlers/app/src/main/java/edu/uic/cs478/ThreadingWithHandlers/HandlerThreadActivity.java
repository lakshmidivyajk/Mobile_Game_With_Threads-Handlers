package edu.uic.cs478.ThreadingWithHandlers;

// import edu.uic.cs478.YourName.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class HandlerThreadActivity extends Activity  {

	private Handler mHandler = new Handler() ; // Handler is created by UI Thread and associated with it
	private Handler h=new Handler();
	private Button mButton1 ;
private TextView msger1,msger2,p1,p2,hint,p1guess,p2guess,hint_p2,status;
	private Bitmap mBitmap ;
	private Boolean mBitmapLock = false,mCounterLock=false ;
	public static Handler mHandlerA,mHandlerB;
	//Number for the messages in the handlers
	public static final int INIT=0;
	public static final int ASGUESS=1;
	public static final int BSGUESS=2;
	public static final int BsTURN=3;
	public static final int AsTURN=4;
	public static final int ARESPONSE=5;
	public static final int BRESPONSE=6;



	public static final int NOT=20;//Total number of turns required, if the game will be a draw

	public static int a_counter=1,b_counter=1;
	public static List<Integer> AguessedNumbers=new ArrayList<Integer>();
//	private final boolean counter_lock=false;

	public static String hint_res="";

	//Threads
	Thread t1 = new Thread(new P1()) ;
	Thread t2 = new Thread(new P2()) ;

	public static final String TAG="HandlerThreadActivity";

	public static boolean state_counter=true;
	public static  int secret_number_B=generateNumber(4);
	public static  int secret_number_A=generateNumber(4);

	public static int no_of_turns=0;
	public static int array_pointer=1;

	public static String aguess,bguess;

	public static int win_a=0;
	public static int win_b=0;
	public static boolean first=true;



	public Bundle bb=new Bundle();
	public HandlerThreadActivity() {
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Setting up the UI elements:
        mButton1 = (Button) findViewById(R.id.button1) ;
		//mButton2 = (Button) findViewById(R.id.button2) ;
        msger1=(TextView)findViewById(R.id.msger1);
        msger2=(TextView)findViewById(R.id.msger2);
        p1=(TextView)findViewById(R.id.p1);
        p2=(TextView)findViewById(R.id.p2);
		hint=(TextView)findViewById(R.id.hint);
		hint.setMovementMethod(new ScrollingMovementMethod());
		p1guess=(TextView)findViewById(R.id.p1guess);
		p1guess.setMovementMethod(new ScrollingMovementMethod());
		p2guess=(TextView)findViewById(R.id.p2guess);
		p2guess.setMovementMethod(new ScrollingMovementMethod());
		hint_p2=(TextView)findViewById(R.id.hint_p2);
		hint_p2.setMovementMethod(new ScrollingMovementMethod());
		status=(TextView)findViewById(R.id.status);
		final String snA=""+secret_number_A;
		final String snB=""+secret_number_B;
		//UI handler to handle the communication between both the threads t1,t2
		mHandler=new Handler(){
			@Override
			public void handleMessage(Message msg){
				int what=msg.what;
				switch(what){

					//ASGUESS is used when player 1 has guessed and it sets the response or the result of the
					//computation in the UI thread in the corresponding textviews.
					//Also sends the response to the A thread or the player1's thread
					case ASGUESS: synchronized (mCounterLock){

						bb=msg.getData();
						int guessed_number=bb.getInt("ASGUESS");//get the data from the message
						//Set them on the UI
						String snA=""+secret_number_A;
						String gno=""+guessed_number;

						Log.i(TAG,""+guessed_number);
						 aguess=""+guessed_number;
						 if(aguess.length()==3){
						 	aguess="0"+aguess;
						 }
						p1guess.append("\n"+aguess);
						hint_res=getHint(snB,aguess);

						//Setting the UI elements on the UI thread
						h.postDelayed(new Runnable() {
							@Override
							public void run() {

								Message msg2=mHandlerA.obtainMessage(HandlerThreadActivity.BRESPONSE);
								bb.putString("RESPONSE",hint_res);//Sending the response to P1 for its guess
								msg2.setData(bb);//Setting the data
								mHandlerA.sendMessage(msg2);

								hint.append("\n"+hint_res);

								//Checking if P1 has won the game to decide the next turn.
								if(hint_res.contains("4:")){
									win_a=1;
									status.setText("A GUESSED THE NUMBER RIGHTLY");


								}
								//If P2 has not yet won the game.
								if(win_b==0){
									Log.i(TAG,"B'S TURN###P1");
									Message msg1=mHandlerB.obtainMessage(HandlerThreadActivity.BsTURN);
									mHandlerB.sendMessageDelayed(msg1,2000);
								}else{//Else if it has won the game
									if(first)
									{
										no_of_turns++;
										first=false;
									}
									Log.i(TAG,"TURNS"+no_of_turns);
									//no_of_turns++;
									if(no_of_turns<NOT){

									Log.i(TAG,"A'S TURN####P1");
									Message msg1=mHandlerA.obtainMessage(HandlerThreadActivity.AsTURN);
									mHandlerA.sendMessageDelayed(msg1,2000);
									no_of_turns++;
									array_pointer++;}
								}

							}
						}, 2000);//Delaying by 2000 ms.






						break;

					}

						//BSGUESS is used when player 2 has guessed and it sets the response or the result of the
						//computation in the UI thread in the corresponding textviews.
						//Also sends the response to the B thread or the player2's thread
					case BSGUESS: synchronized (mCounterLock){

						bb=msg.getData();
						int guessed_number=bb.getInt("BSGUESS");//Get the data
						//Setting the data on the UI thread
						String gno=""+guessed_number;
						Log.i(TAG,""+guessed_number);
						 bguess=""+guessed_number;
						if(bguess.length()==3){
							bguess="0"+bguess;
						}
						p2guess.append("\n"+bguess);
						//Setting the UI elements
						h.postDelayed(new Runnable() {
							@Override
							public void run() {
								hint_res=getHint(snA,bguess);
								Message msg2=mHandlerB.obtainMessage(HandlerThreadActivity.ARESPONSE);//Sending the response to player2 for its guess
								bb.putString("RESPONSE",hint_res);
								msg2.setData(bb);
								mHandlerB.sendMessage(msg2);

								hint_p2.append("\n"+hint_res);
								//Checking it Player 2 has won the game
								if(hint_res.contains("4:")){
									win_b=1;
									status.setText("B GUESSED THE NUMBER RIGHTLY");

								}
								no_of_turns++;//Getting the number of moves.
								Log.i(TAG,"TURNS"+no_of_turns);

								if(no_of_turns<NOT) //HERE I'M CHECKING THE NUMBER OF MOVES
								{
									//Checking if P2 has won the game to decide the next turn.
									//If P1 has not yet won the game
									if(win_a==0){
										Log.i(TAG,"A'S TURN");
										Message msg1=mHandlerA.obtainMessage(HandlerThreadActivity.AsTURN);//P1'S turn
										//bb.putString("")
										mHandlerA.sendMessageDelayed(msg1,2000);
										array_pointer++;
									}else if(win_b==0){
										Log.i(TAG,"B'S TURN+****");//else P2's turn
										Message msg1=mHandlerB.obtainMessage(HandlerThreadActivity.BsTURN);
										//bb.putString("")
										mHandlerB.sendMessageDelayed(msg1,2000);
										array_pointer++;
									}


								}
								//If no one has won the game, in 20 moves the game is a draw
								if(no_of_turns==NOT&&win_a==0&&win_b==0){
									Log.i(TAG,"GAME IS A DRAW");
									status.setText("GAME IS A DRAW");
								}





							}
						}, 2000);//Delay by 2000 ms







						/*if(no_of_turns==3){
							t1.stop();t2.stop();
						}*/




						break;

					}



				}



			}
		};

		//What to do when the button mButton1 is clicked:
        
        mButton1.setOnClickListener(new View.OnClickListener() {

        	       public void onClick(View v) {
        	       	//Click the button to start the game
        	       	Log.i(TAG,"INSIDE BUTTON");

							if(!state_counter)
							{    t1.interrupt();
								t2.interrupt(); state_counter=false;
								if(t1.isAlive()){
									Log.i(TAG,"NOT INTERRUPTED");
								}
								t1=new Thread(new P1());
								t2=new Thread(new P2());
							}
							p1.setText("");
							p2.setText("");
							p1guess.setText("");
							p2guess.setText("");
							hint.setText("");
							hint_p2.setText("");
							status.setText("");
							no_of_turns=0;


						   t1.start();
						   t2.start();







					   Log.i(TAG,"START");

        	       }
        });
        //terminator();

        

    }




//Player1 class
	public class P1 implements Runnable
	{

		//Player1 thread
		public String response_from_B="";
		//Set the player1's secret number:
		@Override
		public void run(){
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					synchronized (mBitmapLock){
						Log.i(TAG,"Setting the SN for P1");
						p1.setText(""+HandlerThreadActivity.secret_number_A);
					}
				}
			});
			//Creates a looper for player1
			if(Looper.myLooper()==null){
				Looper.prepare();
			}

			try {  Thread.sleep(2000); }
			catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }
			//As P1 is going to guess send the message ASGUESS to the mHandler

			Message m=mHandler.obtainMessage(HandlerThreadActivity.ASGUESS);

			bb.putInt("ASGUESS",guessGeneratorA()); //put the guessed number in a bundle
			a_counter++;b_counter--;
			m.setData(bb);
			mHandler.sendMessageDelayed(m,2000);//Delay the message by 2000ms


			//P1 thread's handler
			mHandlerA=new Handler(){
				@Override
				public void handleMessage(Message msg){
					int what=msg.what;
					switch(what){
						//Handles when AsTURN message is recieved from the UI thread
						case AsTURN:Message m=mHandler.obtainMessage(HandlerThreadActivity.ASGUESS);
							bb.putInt("ASGUESS",guessGeneratorA());//generate a number to guess, send it to the UI thread
							//a_counter--;b_counter++;
							m.setData(bb);
							mHandler.sendMessageDelayed(m,2000);break;
						//Handled when BSRESPONSE message is recieved from the UI thread
						case BRESPONSE:
							bb=msg.getData();
							response_from_B=bb.getString("RESPONSE");//Retrieve the response
							Log.i(TAG,response_from_B);
							break;


							//do something

					}



				}
			};

			Looper.loop();
			Looper.myLooper().quit();

		}


	}
//Player 2 class
	public class P2 implements Runnable
	{
		//Player 2's thread
		public int secret_number_B=2017;
		String hint_res;
		String response_from_A;
		//Set the Player 2's secret number on the UI thread.

		@Override
		public void run()
		{
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					synchronized (mBitmapLock){ //Acquire the lock
						Log.i(TAG,"Setting the SN for P2");
						p2.setText(""+HandlerThreadActivity.secret_number_B);//Set the text
					}
				}
			});

			//Create a looper for this thread
			if(Looper.myLooper()==null){
				Looper.prepare();

			}

			//mHandlerB is the handler of the Player 2 thread
			mHandlerB=new Handler(){
				@Override
				public void handleMessage(Message msg){
					int what=msg.what;
					switch(what){
						//Handles when BsTURN message is recieved from the UI thread
						case BsTURN:Message m=mHandler.obtainMessage(HandlerThreadActivity.BSGUESS);
							bb.putInt("BSGUESS",guessGeneratorB());
							//a_counter--;b_counter++;
							m.setData(bb);
							mHandler.sendMessageDelayed(m,2000);break;
							//Handles when ARESPONSE is recieved from the UI Thread
						case ARESPONSE:
							bb=msg.getData();
							response_from_A=bb.getString("RESPONSE");
							Log.i(TAG+"##",response_from_A);
							break;



					}



				}
			};

			Looper.loop();

			Looper.myLooper().quit();
		}



	}



//The following function is used to determine the number of digits in the same location and number of digits in different location.
	public  String getHint(String secret, String guess) {
		int countBull=0;
		int countCow=0;

		HashMap<Character, Integer> map = new HashMap<Character, Integer>();
		/*try {  Thread.sleep(4000);Log.i(TAG,"SLEEPING"); }
		catch (InterruptedException e) { System.out.println("Thread interrupted!") ; }*/

		//check bull
		for(int i=0; i<secret.length(); i++){
			char c1 = secret.charAt(i);
			char c2 = guess.charAt(i);
			if(c1==c2){
				countBull++;
			}else{
				if(map.containsKey(c1)){
					int freq = map.get(c1);
					freq++;
					map.put(c1, freq);
				}else{
					map.put(c1, 1);
				}
			}
		}
		//check cow
		for(int i=0; i<secret.length(); i++){
			char c1 = secret.charAt(i);
			char c2 = guess.charAt(i);

			if(c1!=c2){
				if(map.containsKey(c2)){
					countCow++;
					if(map.get(c2)==1){
						map.remove(c2);
					}else{
						int freq = map.get(c2);
						freq--;
						map.put(c2, freq);
					}
				}
			}
		}

		return countBull+":"+countCow;//bull means same cow means different


	}
	//This function intially generates the secret numbers without reptition of numbers
	public static int generateNumber(int length){
		String result = "";
		int random;
		while(true){
			random  = (int) ((Math.random() * (10 )));
			if(result.length() == 0 && random == 0){//when parsed this insures that the number doesn't start with 0
				random+=1;
				result+=random;
			}
			else if(!result.contains(Integer.toString(random))){//if my result doesn't contain the new generated digit then I add it to the result
				result+=Integer.toString(random);
			}
			if(result.length()>=length){//when i reach the number of digits desired i break out of the loop and return the final result
				break;
			}
		}

		return Integer.parseInt(result);
	}
	//This guess generator uses list type, shuffle function to create the guessing numbers for player1 without reptition of digits

	public static int  guessGeneratorA(){
		List<Integer> numbers;numbers=new ArrayList<>();
		for(int i = 0; i < 10; i++){
			numbers.add(i);
		}
		Collections.shuffle(numbers);
		String result = "";
		for(int i = 0; i < 4; i++){
			result += numbers.get(i).toString();
		}
		return Integer.parseInt(result);
	}
	//This guess generator uses hash set to check if the digits are non-reptitive and generates unique numbers for player2
	public static int guessGeneratorB(){
		Random r = new Random();
		Set<Integer> s = new HashSet<Integer>();
		String str="";
		//List<Integer> a=new ArrayList<Integer>();
		while (s.size() < 4) {
			s.add(r.nextInt(9));
		}
		Iterator iter = s.iterator();
		while (iter.hasNext()) {
			// System.out.println(iter.next());
			str+=iter.next();

		}

		return Integer.parseInt(str);
	}



	@Override
	protected void onResume() {

		super.onResume();
		this.onCreate(null);
	}








}

    

