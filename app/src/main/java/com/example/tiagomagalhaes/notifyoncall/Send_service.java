package com.example.tiagomagalhaes.notifyoncall;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by tiagomagalhaes on 12/02/2017.
 */

public class Send_service extends IntentService {
    public Send_service(String name) {
        super(Send_service.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
