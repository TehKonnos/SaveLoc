package com.example.saveloc;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    Button saveBtn;
    double latitude;
    double longitude;
    String markertxt,countryName,locality;
    public static FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        db =FirebaseFirestore.getInstance(); //Ετοίμασα την Firebase

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Ελέγχω αν έχω πάρει permission απ το χρήστη
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //Αν έχω διαθέσημη τοποθεσία μέσω Internet πέρνω την τοποθεσία
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //Διαβάζω το Latitude και το Longitude
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    //Δημιουργώ το LatLng του σημείου όπου βρίσκεται ο χρήστης
                    LatLng latLng = new LatLng(latitude,longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    //Δημιουργώ το Marker
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);//Πέρνω το πρώτο αποτέλεσμα της τοποθεσίας των συντεταγμένων
                        locality =addressList.get(0).getLocality();//Περιοχή/πόλη
                        countryName = addressList.get(0).getCountryName();//Χώρα
                        markertxt =locality +" , "+ countryName;//Τα εμφανίζω στον τίτλο
                        mMap.addMarker(new MarkerOptions().position(latLng).title(markertxt)); //Δημιουργώ το Marker
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f)); //Βάζω την κάμερα πάνω απ το σημείο
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });
            //Αν δεν έχω διαθέσιμη τοποθεσία μέσω Internet, τότε ψάχνω να δώ αν την έχω απο GPS
        }else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //Διαβάζω το Latitude και το Longitude
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    //Δημιουργώ το LatLng του σημείου όπου βρίσκεται ο χρήστης
                    LatLng latLng = new LatLng(latitude,longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    //Δημιουργώ το Marker
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);//Πέρνω το πρώτο αποτέλεσμα της τοποθεσίας των συντεταγμένων
                        locality =addressList.get(0).getLocality();//Περιοχή/πόλη
                        countryName = addressList.get(0).getCountryName();//Χώρα
                        markertxt =locality +" , "+ countryName;//Τα εμφανίζω στον τίτλο
                        mMap.addMarker(new MarkerOptions().position(latLng).title(markertxt)); //Δημιουργώ το Marker
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10.2f)); //Βάζω την κάμερα πάνω απ το σημείο
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });

        }else{//Αν δεν έχω καμία πρόσβαση σε τοποθεσία
            Toast.makeText(this,"Σφάλμα: Δεν υπάρχη πρόσβαση για τοποθεσία μέσω Ίντερνετ ή GPS",Toast.LENGTH_SHORT).show();
            finish();
        }
        //Βάζω ένα Listener και περιμένω να πατηθεί το κουμπί "Αποθήκευση"
        saveBtn=findViewById(R.id.button);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Όταν πατηθεί, ανοίγω το activity με τα δεδομένα που θα αποθηκεύσω και στέλνω το Longitude και Latitude
                Intent intent = new Intent(MapsActivity.this,SaveActivity.class);
                intent.putExtra("latitude",Double.toString(latitude));
                intent.putExtra("longitude",Double.toString(longitude));
                startActivity(intent);// Ανοίγω το επόμενο Activity
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
