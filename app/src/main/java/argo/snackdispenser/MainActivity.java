package argo.snackdispenser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    final Context context = this;
    GridView gridView;
    ArrayList<Snack> list = new ArrayList<>();
    Button getSnack;
    ImageView imView;
    String[] nameOfSnacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "ePSkWJSub8yTtTxEb41FQf6TGyqMrrwUJLRHO2ii", "3EjhxPiEgqiEW3xOIft9IKR6RP2hVAdcCYOkTBia");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

        // change default font
        ReplaceFont.replaceDefaultFont(this, "DEFAULT", "fonts/Munro.ttf");
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refill was selected
            case R.id.action_refill:
                showRefillDialog();
                break;

            // action with ID action_help was selected
            case R.id.action_help:
                showHelp();
                break;

            // action with ID action_check_stock was selected
            case R.id.action_check_stock:
                showStock();
                break;

            // action with ID action_about was selected
            case R.id.action_about:
                showAbout();
                break;
            default:
                break;
        }

        return true;
    }


    /**
     * Called by initView();
     *
     * This method is used to setup the dialog view when "I'm feeling lucky" button is tapped
     * The method randomly generate an index, and chooses the snack with that index in the Arraylist
     * If the snack is available, then the dialog continues to ask the user whether he wants it or not
     *      If yes, then decrement the snack's stock, and pop out the snack's image below
     *      If not, then nothing happens
     * If the snack is out of stock, tell the user what the snack is, and remind him to refill the dispenser
     * If a beer is chosen and the user wants it, then refuse to pop it out and tell the user not
     * to drink during working hours.
     *
     */
    private void dialogSetup() {
        //setup the getSnack button
        getSnack = (Button) findViewById(R.id.getSnack);
        getSnack.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //randomly generate a snack number
                int snNum =(int) (Math.random() * list.size());
                final Snack curSnack = list.get(snNum);

                //pop up alert
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                //set dialog message
                if (curSnack.getQuat() > 0)
                {
                    alertDialogBuilder
                            .setMessage("The Snack Dispenser popped out a " + curSnack.getName().toLowerCase()
                                    + "! Do you want it?")
                            .setCancelable(false)
                            .setPositiveButton("Yes!", new DialogInterface.OnClickListener(){
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    //if it's a beer, then refuse to pop it
                                    if (curSnack.getName().equals("Beer")){
                                        AlertDialog popout = new AlertDialog.Builder(context).create();
                                        popout.setMessage("No drinking during working hours!");
                                        popout.setButton("Fine...", new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        popout.show();
                                    }
                                    else {
                                        //if the user want the snack, then decrement the quantity
                                        int curQuat = curSnack.getQuat() - 1;
                                        curSnack.setQuat(curQuat);

                                        //refresh
                                        gridView.setAdapter(new MyAdapter(context, list));

                                        //initialize imageView to show popped snack
                                        imView = (ImageView)findViewById(R.id.imageView);
                                        imView.setImageResource(curSnack.getIconid());
                                        imView.setVisibility(View.VISIBLE);

                                        // tell the user to take the snack away
                                        AlertDialog popout = new AlertDialog.Builder(context).create();
                                        popout.setMessage("Tap on the snack below to take it away! Enjoy!");
                                        popout.setButton("OK!", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                        popout.show();

                                        // make food pic disappear when clicked
                                        if (imView != null) {
                                            imView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imView.setVisibility(View.INVISIBLE);
                                                }
                                            });
                                        }
                                    }
                                    dialog.dismiss();
                                }

                            })
                            .setNegativeButton("Nah", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                } else {
                    alertDialogBuilder
                            .setMessage("The Snack Dispenser wanted to give you a "
                                    + curSnack.getName().toLowerCase()
                                    + ", but we've run out of those! Remember to refill the Dispenser!")
                            .setNeutralButton("Fine...", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                }

                //create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                //show it
                alertDialog.show();
            }
        });


    }

    /**
     * Called by onCreate();
     *
     * Initialize the app.
     * First, call popSnack() to populate the Arraylist with snacks
     * Then setup the GridView using custom adapter
     * Then call dialogSetup() to setup the button click action
     *
     */
    private void initView() {
        //setup the snacks
        popSnack();

        //setup gridView
        gridView = (GridView) findViewById(R.id.gridView);

        final ListAdapter adapter = new MyAdapter(this, list);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        ((TextView) v.findViewById(R.id.sn_label))
                                .getText(), Toast.LENGTH_SHORT).show();
            }
        });

        // setup the pop-up dialog when button is clicked
        dialogSetup();
    }

    /**
     * Called by initView();
     *
     * Create a bunch of snacks and add them into the Arraylist
     * Then store the names of the snacks into a String array for future use in Refill function
     *
     */
    private void popSnack(){
        //setup a bunch of snacks
        Snack banana = new Snack("Banana", 3, R.drawable.bananas);
        Snack beer = new Snack("Beer", 1, R.drawable.beer);
        Snack cake = new Snack("Cake", 2, R.drawable.cake);
        Snack chocolate = new Snack("Chocolate", 2, R.drawable.chocolate);
        Snack coffee = new Snack("Coffee", 5, R.drawable.coffee);
        Snack cookies = new Snack("Cookie", 2, R.drawable.cookies);
        Snack icecream = new Snack("Ice Cream", 0, R.drawable.icecream);
        Snack Milk = new Snack("Milk", 4, R.drawable.milk);
        Snack mochi = new Snack("Mochi", 3, R.drawable.mochi);
        Snack pudding = new Snack("Pudding", 4, R.drawable.pudding);
        Snack sushi = new Snack("Sushi", 1, R.drawable.sushi);
        Snack Taco = new Snack("Taco", 1, R.drawable.taco);

        //add them to the list
        list.add(banana);
        list.add(beer);
        list.add(cake);
        list.add(chocolate);
        list.add(coffee);
        list.add(cookies);
        list.add(icecream);
        list.add(Milk);
        list.add(mochi);
        list.add(pudding);
        list.add(sushi);
        list.add(Taco);

        nameOfSnacks = new String[list.size()];
        for (int i = 0; i < nameOfSnacks.length; i++) {
            nameOfSnacks[i] = list.get(i).getName();
        }
    }


    // all methods below are menu functions called by onOptionsItemSelected;

    /**
     * This method calculates the number of snacks out of stock and the number of snacks that have
     * less than three in stock, and generate message based on stock situation
     */
    private void showStock() {
        // string and int to store the names and number of snacks (almost) out of stock
        StringBuilder outOfStock = new StringBuilder();
        StringBuilder lessThanThree = new StringBuilder();
        int outOfStockNum = 0, lessThanThreeNum = 0;

        // check stock
        for(int i = 0; i < list.size(); i++) {
            Snack curSnack = list.get(i);
            int curQuat = curSnack.getQuat();
            if (curQuat == 0) {
                outOfStock.append(curSnack.getName() + "\n");
                outOfStockNum++;
            } else if (curQuat > 0 && curQuat < 3) {
                lessThanThree.append(curSnack.getName() + "\n");
                lessThanThreeNum++;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Check Stock");

        // set message depending on situation of stock
        if (outOfStockNum == 0 && lessThanThreeNum == 0)
            builder.setMessage("Your stock is still pretty full, no worries!");
        else{
            StringBuilder message = new StringBuilder();
            if (outOfStockNum != 0) {
                message.append("There are " + outOfStockNum + " snacks out of stock, including:\n" +
                        outOfStock.toString() + "\n");
            }
            if (lessThanThreeNum != 0) {
                message.append("There are " + lessThanThreeNum + " snacks that have less than three in stock, including:\n" +
                        lessThanThree.toString());
            }
            builder.setMessage(message.toString());
        }

        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    /**
     * This method uses NumberPicker to let the user choose a snack and an amount to refill the
     * dispenser, and reminds the user how many of that snack is in stock after "Refill" button
     * is tapped.
     */
    private void showRefillDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.setTitle("Choose the snack to refill: ");
        dialog.setContentView(R.layout.refilldialog);

        // get the buttons
        Button pick = (Button) dialog.findViewById(R.id.refill_set);
        Button cancel = (Button) dialog.findViewById(R.id.refill_cancel);

        // get the NumberPickers
        final NumberPicker snackPicker = (NumberPicker) dialog.findViewById(R.id.refill_picker);
        final NumberPicker amountPicker = (NumberPicker) dialog.findViewById(R.id.refill_amount);

        // the max value of available choices should be the number of snacks
        snackPicker.setMaxValue(list.size() - 1);
        snackPicker.setMinValue(0);
        snackPicker.setDisplayedValues(nameOfSnacks);
        snackPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {}
        });

        // to make sure that the snacks in the dispenser are fresh, I decided that the max number
        // you can add at one time is 30
        amountPicker.setMaxValue(30);
        amountPicker.setMinValue(1);
        amountPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {}
        });

        pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snack chosenSnack = list.get(snackPicker.getValue());
                chosenSnack.setQuat(chosenSnack.getQuat() + amountPicker.getValue());

                // refresh
                gridView.setAdapter(new MyAdapter(context, list));

                // refill success alert
                final AlertDialog successAlert = new AlertDialog.Builder(context).create();
                successAlert.setMessage("Successfully Refilled! Now the Dispenser has "
                        + chosenSnack.getQuat() + " "
                        + chosenSnack.getName().toLowerCase()
                        + " in stock!");
                successAlert.setButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        successAlert.dismiss();
                    }

                });
                successAlert.show();
                dialog.dismiss();
            }
        });

        // setup the onclicklistener on cancel button
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    /**
     * This method tells the user how to use this app
     */
    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Help")
                .setMessage("This is a random snack dispenser.\n" +
                        "Tap on the \"I'm Feeling Lucky\" button to get your randomly chosen snack.\n" +
                        "If the snack is in stock, you can decide whether you want it or not.\n" +
                        "If the snack is in stock and you want it, then the snack will appear in the container below. You can take it away by tapping on it.\n" +
                        "If the snack is out of stock, then you can either try again or refill the dispenser.\n" +
                        "Check Stock will tell you if there's any snack that is (almost) out of stock.\n" +
                        "No drinking during working hours!")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method shows the About information
     */
    private void showAbout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("About")
                .setMessage("This Snack Dispenser is built by Boya Yan for the snack lovers at Argo.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
