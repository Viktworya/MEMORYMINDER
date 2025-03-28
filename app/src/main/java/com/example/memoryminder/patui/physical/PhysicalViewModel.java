package com.example.memoryminder.patui.physical;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PhysicalViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PhysicalViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Modulefragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}