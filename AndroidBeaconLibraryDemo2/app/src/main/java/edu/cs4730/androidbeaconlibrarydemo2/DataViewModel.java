package edu.cs4730.androidbeaconlibrarydemo2;

import android.app.Application;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

class DataViewModel extends AndroidViewModel {
    private MutableLiveData<String> item;
    public DataViewModel(@NonNull Application application) {
        super(application);
    }

    /*
    private MutableLiveData<String> item;
    private MutableLiveData<Collection<Beacon>> mlist;

    public DataViewModel(Application application) {
        super(application);
        item = new MutableLiveData<String>();
        mlist = new MutableLiveData<Collection<Beacon>>();
    }


    MutableLiveData<Collection<Beacon>> getBeaconlist() {
       // return mlist;
        return null;
    }

    void setMlist(Collection<Beacon> n) {
       // mlist.setValue(n);
    }

    Collection<Beacon> getMlist() {
      //  return mlist.getValue();
        return null;
    }

*/
    LiveData<String> getItemLD() {

        return item;
    }

    String getItem() {
        return item.getValue();
    }

    void setItem(String n) {
        item.setValue(item.getValue() + item + "\n");

    }


}
