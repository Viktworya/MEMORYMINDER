package com.example.memoryminder.ui.module;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ModuleViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ModuleViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Modulefragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}