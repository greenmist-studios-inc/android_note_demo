package com.greenmiststudios.androiddemo.activity;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

import com.greenmiststudios.androiddemo.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    @Rule
    public ActivityTestRule<HomeActivity> mActivityTestRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void homeActivityTest() {
        ViewInteraction appCompatButton = onView(
allOf(withId(android.R.id.button1), withText("OK"),
withParent(allOf(withId(R.id.buttonPanel),
withParent(withId(R.id.parentPanel)))),
isDisplayed()));
        appCompatButton.perform(click());
        
        ViewInteraction floatingActionButton = onView(
allOf(withId(R.id.fab), isDisplayed()));
        floatingActionButton.perform(click());
        
        ViewInteraction appCompatEditText = onView(
allOf(withId(R.id.title),
withParent(withId(R.id.text_input_layout)),
isDisplayed()));
        appCompatEditText.perform(replaceText("title"));
        
        ViewInteraction appCompatEditText2 = onView(
allOf(withId(R.id.note),
withParent(withId(R.id.text_input_layout2)),
isDisplayed()));
        appCompatEditText2.perform(replaceText("note"));
        
        ViewInteraction appCompatButton2 = onView(
allOf(withId(R.id.add_location), withText("Add Location"), isDisplayed()));
        appCompatButton2.perform(click());
        
        ViewInteraction floatingActionButton2 = onView(
allOf(withId(R.id.add), isDisplayed()));
        floatingActionButton2.perform(click());
        
        ViewInteraction floatingActionButton3 = onView(
allOf(withId(R.id.save), isDisplayed()));
        floatingActionButton3.perform(click());
        
        ViewInteraction textView = onView(
allOf(withId(R.id.title), withText("title"),
withParent(withId(R.id.title_frame)),
isDisplayed()));
        textView.check(matches(withText("title")));
        
        }
}
