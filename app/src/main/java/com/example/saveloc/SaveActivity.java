package com.example.saveloc;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.GeoPoint;


public class SaveActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener{

    SensorManager DeviceSensorManager;
    Sensor gravityS,tempS,humidS;
    String gravityRw,gravityRh,tempR,humidR,result,latitude,longitude;
    EditText description;
    TextView gravW,gravH,tempRtxt,humidRtxt,lati,longi;
    Spinner spinner;
    float markerColor;
    Button save2Btn;
    GeoPoint geoPoint;
    float[] markerColors={180.0f,210.0f,240.0f,120.0f,0.0f,330.0f,30.0f,60.0f,270.0f,300.0f}; //Η χρωμματική παλέττα για τα Markers. Τα hue αποθηκευμένα ως float τιμες

    ArrayAdapter<CharSequence> adapter;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        //Πέρνω τα Latitude και Longitude που μου έστειλε το MapsActivity
        Intent intent = getIntent();
        latitude = intent.getStringExtra("latitude");
        longitude =intent.getStringExtra("longitude");
        //Το σημείο σαν GeoPoint, ώστε να το αποθηκεύσω κατευθείαν στη βάση και να το επεξεργαστώ από την άλλη εφαμογή
        geoPoint = new GeoPoint(Double.parseDouble(latitude),Double.parseDouble(longitude));

        //Συνδέω μεταβλητές με αντικείμενα του xml
        lati=findViewById(R.id.latTxt);
        longi=findViewById(R.id.longTxt);
        gravW=findViewById(R.id.gravW);
        gravH=findViewById(R.id.gravH);
        tempRtxt=findViewById(R.id.tempTxt);
        humidRtxt=findViewById(R.id.humidTxt);

        //Βάζουμε listener στο κουμπί, που όταν πατηθεί θα αποθηκευτούν τα στοιχεία στην firebase
        save2Btn = findViewById(R.id.SaveBtn2);
        save2Btn.setOnClickListener(this);

        //Εδώ είναι η περιγραφή που όρισε ο χρήστης.
        description=findViewById(R.id.descText);

        //Εμφανίζω το αποθηκευμένο Latitude
        result ="X: "+latitude;
        lati.setText(result);
        //Εμφανίζω το αποθηκευμένο Longitude
        result="Y: "+longitude;
        longi.setText(result);

        //Δημιουργώ ένα Sensor Manager ο οποίος θα έχει πρόσβαση στους αισθητήρες της συσκευής
        DeviceSensorManager =(SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Δημιουργώ ένα spinner για να επιλέξει ο χρήστης το χρώμα του Marker
        spinner =findViewById(R.id.appCompatSpinner);
        adapter = ArrayAdapter.createFromResource(this,R.array.markerColors,R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Η θέση του χρώμματος στον spinner είναι και η θέση του float χρώμματος στο
                //άλλο πρόγραμμα που θα το έχω αποθηκεύση σε ένα πίνακα.
                markerColor= markerColors[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        DeviceSensorManager.unregisterListener(this);

    }

    protected void onResume(){
        //Ελέγχω τις τιμές που μου δίνουν οι Sensors και αποθηκεύω τα αποτελέσματα, αν αυτά υπάρχουν.
        super.onResume();
        gravityS=DeviceSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        tempS = DeviceSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humidS = DeviceSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if(gravityS!=null)
            DeviceSensorManager.registerListener(this,gravityS,SensorManager.SENSOR_DELAY_UI);
        else{
            gravityRw="-";
            gravityRh="-";
        }

        if(tempS!=null)
            DeviceSensorManager.registerListener(this,tempS,SensorManager.SENSOR_DELAY_UI);
        else
            tempR="-";

        if(humidS!=null)
            DeviceSensorManager.registerListener(this,humidS,SensorManager.SENSOR_DELAY_UI);
        else
            humidR="-";

        setTexts(); //Εμφανίζω τις τιμές στην διεπαφή

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Ελέγχω αν έγινε κάποια αλλαγή Real-Time από τους Sensors.
        switch (sensorEvent.sensor.getType()){
            case Sensor.TYPE_GRAVITY:
                gravityRw= Float.toString(sensorEvent.values[0]);
                gravityRh=Float.toString(sensorEvent.values[1]);
                break;
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                tempR =Float.toString( sensorEvent.values[0]);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                humidR = Float.toString(sensorEvent.values[0]);
                break;
        }

        setTexts(); //Εμφανίζω τις τιμές στην διεπαφή
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setTexts(){ //Μέθοδος που εμφανίζει τις τιμές από τους Sensors στη διεπαφή
        result="Gravity weight: "+ gravityRw;
        gravW.setText(result);

        result="Gravity height: "+gravityRh;
        gravH.setText(result);

        result="Θερμοκρασία: "+tempR;
        tempRtxt.setText(result);

        result="Υγρασία: "+humidR;
        humidRtxt.setText(result);
    }

    @Override
    public void onClick(View view) {
        //Όταν πατηθεί το κουμπί για αποθήκευση στη βάση, ελέγχω αν πληρούνται όλες οι προϋποθέσεις και προχωράω στην αποθήκευση.
        String string = description.getText()+"";
        if(countWords(string)>10)
            Toast.makeText(this,"Η πρόταση σου δεν πρέπει να ξεπερνάει το όριο των 10 λέξεων.",Toast.LENGTH_SHORT).show();
        else if(countWords(string)==0)
            Toast.makeText(this,"Πρέπει να γράψεις κάποια περιγραφή (Μέχρι 10 λέξεις)",Toast.LENGTH_SHORT).show();
        else
            saveAll(); //Προχωράω στην αποθήκευση
    }

    //Μέθοδος που σπάει το string και βρίσκει απο πόσες λέξεις αποτελείται η πρόταση που δίνουμε
    public static int countWords(String s){

        int wordCount = 0;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // Αν ένας χαρακτήρας είναι γράμμα τοτε, word = true.
            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
                word = true;
                //Αν ο χαρακτήρας δεν είναι γράμμα αλλά υπάρχουν λέξεις πιο πρίν τότε ολοκληρώθηκε μία λέξη
            } else if (!Character.isLetter(s.charAt(i)) && word) {
                wordCount++;
                word = false;
                //Η τελευταία λεξη που τελείνει με τελεία, ή κάποιο μη-γραμμα.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }

    void saveAll(){
        //Εδώ στέλνω τις πληροφορίες στη βάση Firestore
        try{
            //Αντικείμενο location που θα περιέχει όλες τις τιμές που θα αποθηκεύσω στη Firestore
            Location location = new Location();
            location.setGeopoint(geoPoint);
            location.setGravH(gravityRh);
            location.setGravW(gravityRw);
            location.setComment(description.getText()+"");
            location.setMarkerColor(markerColor);
            location.setHumidR(humidR);
            location.setTempR(tempR);

            //Εκχωρώ τις τιμές στη βάση
            MapsActivity.db.collection("Locations").document().set(location);

            Toast.makeText(this,"Το σημείο αποθηκέυτικε επιτυχώς!",Toast.LENGTH_LONG).show(); //Ενημερώνουμε τον χρήστη ότι όλα πήγαν καλά!
            finish(); //Κλείνουμε το Activity
        }catch(Exception e){
            Toast.makeText(this,"Σφάλμα! Τα στοιχεία δεν αποθηκεύτηκαν στη βάση.",Toast.LENGTH_LONG).show(); //Ενημερώνουμε τον χρήστη ότι κάτι πήγε λάθος
            Log.e("Saving Location: ",""+e);
        }


    }
}
