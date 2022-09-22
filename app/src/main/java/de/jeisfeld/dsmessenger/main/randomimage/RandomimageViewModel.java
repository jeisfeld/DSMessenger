package de.jeisfeld.dsmessenger.main.randomimage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RandomimageViewModel extends ViewModel {

	private final MutableLiveData<String> mText;

	public RandomimageViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("This is gallery fragment");
	}

	public LiveData<String> getText() {
		return mText;
	}
}