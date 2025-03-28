package com.example.memoryminder.patui.cognitive;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CognitiveViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CognitiveViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Register fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}