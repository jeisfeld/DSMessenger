package de.jeisfeld.dsmessenger.main.lut;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LutViewModel extends ViewModel {

	private final MutableLiveData<String> mText;

	public LutViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("This is slideshow fragment");
	}

	public LiveData<String> getText() {
		return mText;
	}
}