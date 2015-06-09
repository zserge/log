package trikita.log.example;

import android.app.Activity;
import android.os.Bundle;

import java.util.Date;

import trikita.log.Log;

public class LogExampleActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.useLog(true).usePrintln(false).useFormat(true);
	}

	public void onResume() {
		super.onResume();
		Log.d("onResume()");
		Log.d(null, 1, "foo", new Date());
		Log.d("Hello, %s", "android");
	}
}
