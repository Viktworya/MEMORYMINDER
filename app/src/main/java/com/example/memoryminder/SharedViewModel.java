package com.example.memoryminder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> username = new MutableLiveData<>();
    private final MutableLiveData<String> stage = new MutableLiveData<>();

    public void setUsername(String username) {
        this.username.setValue(username);
    }

    public LiveData<String> getUsername() {
        return username;
    }

    public void setStage(String username) {
        this.stage.setValue(username);
    }

    public LiveData<String> getStage() {
        return stage;
    }
}
