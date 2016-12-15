package stats.aleperf.example.rollingdieprobabilitygame;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/***
 * Simulate the rolling of a six-sided die. The user can choose a number and see the
 * ratio (0 to 1) of victories and the total number of victories over total rolls.
 * Illustrate the concept that a theoretical probability is much more similar to reality
 * when there are enough experiments.
 *
 */


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivityRollingDie";
    private final String TOTAL_ROLLS = "number of rolls";
    private final String TOTAL_VICTORIES = "number of victories";
    private final String RATIO = "ratio";
    private final String IS_ROLLING = "is_rolling";
    private final String NUMBER_CHOSEN = "number chosen";
    private final String LAST_SEEN_FACE = "last_seen_face";
    private final String HAS_WON = "has won";

    //constants for success of last rolling
    private final int START = 0; //never rolled
    private final int LAST_LOOSE = 1; //failed
    private final int LAST_WIN = 2; //success

    private final int INITIAL_GUESS = 1;
    private final int INITIAL_FACE = 6;


    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;

    private Button buttonReset;

    private TextView numberChosenTextView;
    private TextView correctGuesses;
    private TextView percentageVictories;
    private TextView startStopMessage;
    private TextView victoryMessage;

    private AnimationDrawable animation;
    private ImageView imageDie;

    private int numberChosen = INITIAL_GUESS;
    private int totalRolls = 0;
    private int totalVictories = 0;
    private double victoryRatio = 0;
    private int lastSeenFace = INITIAL_FACE;
    private boolean isRolling;
    private int hasWon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        //inflate buttons for choosing number
        button1 = (Button) findViewById(R.id.button_one);
        button2 = (Button) findViewById(R.id.button_two);
        button3 = (Button) findViewById(R.id.button_three);
        button4 = (Button) findViewById(R.id.button_four);
        button5 = (Button) findViewById(R.id.button_five);
        button6 = (Button) findViewById(R.id.button_six);
        // set up listener on buttons
        ButtonChooserListener buttonChooserListener = new ButtonChooserListener();
        button1.setOnClickListener(buttonChooserListener);
        button2.setOnClickListener(buttonChooserListener);
        button3.setOnClickListener(buttonChooserListener);
        button4.setOnClickListener(buttonChooserListener);
        button5.setOnClickListener(buttonChooserListener);
        button6.setOnClickListener(buttonChooserListener);

        //inflate button Reset
        buttonReset = (Button) findViewById(R.id.button_reset);
        //set listener on Reset Button that reset all values to default
        // and stop animation if necessary
        buttonReset.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View view) {
                                               numberChosen = INITIAL_GUESS;
                                               numberChosenTextView.setText("" + numberChosen);
                                               isRolling = false;
                                               totalRolls = 0;
                                               totalVictories = 0;
                                               victoryRatio = 0.0;
                                               correctGuesses.setText("" + totalVictories + "/" + totalRolls);
                                               String ratioString = String.format("%.2f", victoryRatio);
                                               percentageVictories.setText(ratioString);
                                               hasWon = START;
                                               if(animation != null){
                                                   animation.stop();
                                                   animation = null;
                                               }
                                               updateFace(INITIAL_FACE);
                                               lastSeenFace = INITIAL_FACE;

                                               updateVictoryMessage(START);
                                           }
                                       }

        );

        //inflate all remaining views
        numberChosenTextView = (TextView) findViewById(R.id.number_chosen_text_view);
        startStopMessage = (TextView) findViewById(R.id.start_stop_message);
        victoryMessage = (TextView) findViewById(R.id.victory_message);
        correctGuesses = (TextView) findViewById(R.id.correct_guesses_text_view);
        percentageVictories = (TextView) findViewById(R.id.victories_text_view);
        imageDie = (ImageView) findViewById(R.id.die);
        // set up listener on the die image, pressing the image start the roll
        DieRollingListener rollingListener = new DieRollingListener();
        imageDie.setOnClickListener(rollingListener);

        //check if the app is already running and update fields
        if (savedInstanceState != null) {
            numberChosen = savedInstanceState.getInt(NUMBER_CHOSEN, 1);
            totalRolls = savedInstanceState.getInt(TOTAL_ROLLS, 0);
            totalVictories = savedInstanceState.getInt(TOTAL_VICTORIES, 0);
            victoryRatio = savedInstanceState.getDouble(RATIO, 0.0);
            isRolling = savedInstanceState.getBoolean(IS_ROLLING, false);
            lastSeenFace = savedInstanceState.getInt(LAST_SEEN_FACE, 6);
            hasWon = savedInstanceState.getInt(HAS_WON, 1);
            numberChosenTextView.setText("" + numberChosen);
            String ratioString = String.format("%.2f", victoryRatio);
            percentageVictories.setText(ratioString);
            correctGuesses.setText(totalVictories + "/" + totalRolls);

            if (isRolling) {
                //if is rolling, keep the die rolling
                isRolling = false; // set to false so the updateRolling method start the rolling again
                updateRolling();

            } else {
                //if not rolling keep the last seen face of the die
                updateFace(lastSeenFace);
            }

            updateVictoryMessage(hasWon);
        } else { // first start
            imageDie.setBackgroundResource(R.drawable.die6);

        }


    }

    /***
     * Listener for the buttons for choosing the number
     * update the visible TextView numberChosenTextView
     * and the private field numberChosen
     */
    private class ButtonChooserListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            switch (id) {
                case R.id.button_one:
                    numberChosenTextView.setText("1");
                    numberChosen = 1;
                    break;
                case R.id.button_two:
                    numberChosenTextView.setText("2");
                    numberChosen = 2;
                    break;
                case R.id.button_three:
                    numberChosenTextView.setText("3");
                    numberChosen = 3;
                    break;
                case R.id.button_four:
                    numberChosenTextView.setText("4");
                    numberChosen = 4;
                    break;
                case R.id.button_five:
                    numberChosenTextView.setText("5");
                    numberChosen = 5;
                    break;
                case R.id.button_six:
                    numberChosenTextView.setText("6");
                    numberChosen = 6;
                    break;
                default:
                    numberChosenTextView.setText("1");
                    numberChosen = 1;
            }


        }

    }

    /**
     * Listener on the image of the die, start and stop rolling
     */
    private class DieRollingListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            updateRolling();


        }

    }

    /**
     * Start and stop rolling according to the global value isRolling.
     * Update all the textView affected by the rolling
     */
    private void updateRolling() {
        if (!isRolling) {
            //if is not rolling, start rolling
            isRolling = true;
            imageDie.setBackgroundResource(R.drawable.rollin1);
            animation = (AnimationDrawable) imageDie.getBackground();
            animation.start();
            startStopMessage.setText(getString(R.string.stop_rolling));
            victoryMessage.setVisibility(View.INVISIBLE);
        } else {
            //if is rolling, stop rolling and update all field according the result.
            isRolling = false;
            Random random = new Random();
            lastSeenFace = random.nextInt(6) + 1;
            animation.stop();
            updateFace(lastSeenFace);
            animation = null;
            startStopMessage.setText(getString(R.string.start_rolling));
            if (lastSeenFace == numberChosen) {
                totalRolls++;
                totalVictories++;
                victoryMessage.setVisibility(View.VISIBLE);
                victoryMessage.setText(getString(R.string.game_win));
                hasWon = LAST_WIN;

            } else {
                totalRolls++;
                victoryMessage.setVisibility(View.VISIBLE);
                victoryMessage.setText(getString(R.string.game_lose));
                hasWon = LAST_LOOSE;
            }
            correctGuesses.setText("" + totalVictories + "/" + totalRolls);
            double ratio = ((double) totalVictories) / totalRolls;
            victoryRatio = ratio;
            String ratioString = String.format("%.2f", ratio);
            percentageVictories.setText(ratioString);

        }
    }

    /**
     * @param face number corresponding to a face of a die
     *             Update the image of the die to the face corresponding to the parameter face.
     */

    private void updateFace(int face) {

        switch (face) {
            case 1:
                imageDie.setBackgroundResource(R.drawable.die1);
                break;
            case 2:
                imageDie.setBackgroundResource(R.drawable.die2);
                break;
            case 3:
                imageDie.setBackgroundResource(R.drawable.die3);
                break;
            case 4:
                imageDie.setBackgroundResource(R.drawable.die4);
                break;
            case 5:
                imageDie.setBackgroundResource(R.drawable.die5);
                break;
            case 6:
                imageDie.setBackgroundResource(R.drawable.die6);

        }
    }

    private void updateVictoryMessage(int message) {
        switch (message) {
            case LAST_LOOSE:
                victoryMessage.setVisibility(View.VISIBLE);
                victoryMessage.setText(getString(R.string.game_lose));
                break;
            case LAST_WIN:
                victoryMessage.setVisibility( View.VISIBLE);
                victoryMessage.setText(getString(R.string.game_win));
                break;
            default:
                victoryMessage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TOTAL_ROLLS, totalRolls);
        outState.putInt(TOTAL_VICTORIES, totalVictories);
        outState.putDouble(RATIO, victoryRatio);
        outState.putBoolean(IS_ROLLING, isRolling);
        outState.putInt(NUMBER_CHOSEN, numberChosen);
        outState.putInt(LAST_SEEN_FACE, lastSeenFace);
        outState.putInt(HAS_WON, hasWon);

    }
}
