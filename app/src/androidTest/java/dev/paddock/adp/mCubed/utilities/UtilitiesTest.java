package dev.paddock.adp.mCubed.utilities;

import junit.framework.Assert;
import junit.framework.TestCase;

public class UtilitiesTest extends TestCase {
	public void testFormatTimeZeroMillis() {
		String actual = Utilities.formatTime(0);
		Assert.assertEquals("00:00", actual);
	}
	
	public void testFormatTimeOneSecond() {
		String actual = Utilities.formatTime(1000);
		Assert.assertEquals("00:01", actual);
	}
	
	public void testFormatTimeFiftyNineSeconds() {
		String actual = Utilities.formatTime(59000);
		Assert.assertEquals("00:59", actual);
	}
	
	public void testFormatTimeOneMinute() {
		String actual = Utilities.formatTime(60000);
		Assert.assertEquals("01:00", actual);
	}
	public void testFormatTimeOneMinuteOneSecond() {
		String actual = Utilities.formatTime(61000);
		Assert.assertEquals("01:01", actual);
	}
	
	public void testFormatTimeOneMinuteFiftyNineSeconds() {
		String actual = Utilities.formatTime(119000);
		Assert.assertEquals("01:59", actual);
	}
	
	public void testFormatTimeTwoMinutes() {
		String actual = Utilities.formatTime(120000);
		Assert.assertEquals("02:00", actual);
	}
	
	public void testFormatTimeTwoMinutesOneSecond() {
		String actual = Utilities.formatTime(121000);
		Assert.assertEquals("02:01", actual);
	}
	
	public void testFormatTimeLessThanOneHour() {
		String actual = Utilities.formatTime(3599000);
		Assert.assertEquals("59:59", actual);
	}
	
	public void testFormatTimeOneHour() {
		String actual = Utilities.formatTime(3600000);
		Assert.assertEquals("01:00:00", actual);
	}
	
	public void testFormatTimeOneHourOneSecond() {
		String actual = Utilities.formatTime(3601000);
		Assert.assertEquals("01:00:01", actual);
	}
	
	public void testFormatTimeOneHourOneMinuteOneSecond() {
		String actual = Utilities.formatTime(3661000);
		Assert.assertEquals("01:01:01", actual);
	}
}
