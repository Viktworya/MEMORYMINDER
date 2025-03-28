package com.example.memoryminder.patui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<String> username = new MutableLiveData<>();

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public LiveData<String> getUsername() {
        return username;
    }
}
