package io.github.divayprakash.asynctask;

import android.content.Intent;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String slideTitle = getString(R.string.slide1title);
        String slideDescription = getString(R.string.slide1description);
        int slideBackground = getResources().getColor(R.color.slide1background);
        addSlide(AppIntroFragment.newInstance(slideTitle, slideDescription, R.drawable.slide1image, slideBackground));
        slideTitle = getString(R.string.slide2title);
        slideDescription = getString(R.string.slide2description);
        slideBackground = getResources().getColor(R.color.slide2background);
        addSlide(AppIntroFragment.newInstance(slideTitle, slideDescription, R.drawable.slide2image, slideBackground));
        slideTitle = getString(R.string.slide3title);
        slideDescription = getString(R.string.slide3description);
        slideBackground = getResources().getColor(R.color.slide3background);
        addSlide(AppIntroFragment.newInstance(slideTitle, slideDescription, R.drawable.slide3image, slideBackground));
        showSkipButton(false);
        setDepthAnimation();
    }
    @Override
    public void onDonePressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}
