package com.lmy.iconcapturer.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class UpdateTask extends AsyncTask<Context, Integer, List<String>> {

    private UpdateTaskListener updateTaskListener;

    public UpdateTask(UpdateTaskListener updateTaskListener){
        this.updateTaskListener = updateTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected List<String> doInBackground(Context... contexts) {
        return Utils.checkAppUpdateInfo(contexts[0], false);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(List<String> res) {
        super.onPostExecute(res);
        updateTaskListener.onSuccess(res);
    }
}
