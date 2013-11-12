package com.ppakapp.collection;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StringMatchMapTest {
  
  private StringMatchMap<String> map = new StringMatchMap<>();
  
  @BeforeMethod
  public void beforeTest() {
    map.clear();
  }
  
  @Test
  public void testMatch() {
    map.put("foo", "bar");
    
    assertEquals(map.get("foo"), "bar");
    assertEquals(map.get("ffoo"), "bar");
    assertEquals(map.get("fooo"), "bar");
    assertEquals(map.get("ffooo"), "bar");
    assertNull(map.get("fo"));
    assertNull(map.get("oof"));
    assertNull(map.get("bar"));
    
    map.put("bar", "foo");
    
    assertEquals(map.get("bar"), "foo");
  }
  
  @Test
  public void testRankedMatch() {
    map.put("fooo", "bar1");
    map.put("foo", "bar2");
    
    assertEquals(map.get("foo"), "bar2");
    assertEquals(map.get("ffoo"), "bar2");
    assertEquals(map.get("fooo"), "bar1");
    assertEquals(map.get("ffooo"), "bar1");
    assertNull(map.get("fo"));
    assertNull(map.get("oof"));
  }
  
}
