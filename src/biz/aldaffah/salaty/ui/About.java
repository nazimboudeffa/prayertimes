package biz.aldaffah.salaty.ui;

import android.app.Activity;
import android.os.Bundle;

import biz.aldaffah.salaty.R;

public class About extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.about);// TODO create a new layout
		
		/*
		 * Lot of buttons is not good

		Button contactus = (Button) findViewById(R.id.contactUsButton);
		contactus.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL, new String[] { About.this
						.getString(R.string.contactusEmail) });
				i.putExtra(Intent.EXTRA_SUBJECT,
						About.this.getString(R.string.contactusSubject));
				i.putExtra(Intent.EXTRA_TEXT,
						About.this.getString(R.string.contactusBody));
				try {
					startActivity(Intent.createChooser(i, About.this
							.getString(R.string.contactusChooserString)));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(About.this,
							About.this.getString(R.string.contactusFailed),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		*/

	}

}
