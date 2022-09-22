package de.jeisfeld.dsmessenger.main.message;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MessageViewModel extends ViewModel {

	private final MutableLiveData<String> mMessageText;

	public MessageViewModel() {
		mMessageText = new MutableLiveData<>();
		mMessageText.setValue("This is sample text");
	}

	public LiveData<String> getMessageText() {
		return mMessageText;
	}
}