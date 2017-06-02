package com.ericjesse.conni;

import com.ericjesse.conni.http.HttpClient;
import com.ericjesse.conni.http.InvalidRequestException;
import com.ericjesse.conni.processors.TrayIconUpdater;
import com.ericjesse.conni.tasks.CheckTask;

import java.io.IOException;

/**
 * Created by eric on 01/06/2017.
 */
public class Conni {

    public static void main(final String[] args) throws InvalidRequestException, IOException {

        HttpClient httpClient = new HttpClient();
        httpClient.addObserver(new TrayIconUpdater());
        new CheckTask(httpClient).run();
    }
}
