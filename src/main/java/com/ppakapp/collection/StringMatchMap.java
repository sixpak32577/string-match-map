package com.ppakapp.collection;


import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class StringMatchMap<V> extends AbstractMap<String, V> {

  private AtomicInteger RANK = new AtomicInteger(0);
  
  private Entry<V> root = new Entry<>(null, (V) null, 0);
  
  private int size = 0;
  
  
  @Override
  public int size() {
    return size;
  }

  @Override
  public V get(Object key) {
    String k = (String) key;
    RankedValue<V> match = null;
    
    for (int idx = 0; idx < k.length(); idx++) {
      Entry<V> child = root.children.get(k.charAt(idx));
            
      if (child != null && (match == null || (child.rank < match.rank))) {
        match = checkEntryMatch(child, k, idx, match);
      } 
    }
    
    return match == null ? null : match.value;
  }

  @Override
  public V put(String key, V value) {
    return putInEntry(root, key, value);
  }
  
  private RankedValue<V> checkEntryMatch(Entry<V> entry, String key, int keyIdx, RankedValue<V> match) {    
    int len = entry.key.length();
    int remainingKeyLen = key.length() - keyIdx;
    if (len > remainingKeyLen) {
      // too long to be a match
      return match;
    }
    
    if (entry.key.regionMatches(0, key, keyIdx, len)) {
      if (entry.value != null && (match == null || entry.value.rank < match.rank)) {
        match = entry.value;
      }
      
      if (len < remainingKeyLen) {
        Entry<V> child = entry.children.get(key.charAt(keyIdx + len));
        
        if (child != null && (match == null || (child.rank < match.rank))) {
          match = checkEntryMatch(child, key, keyIdx + len, match);
        } 
      }
    }
    
    return match;
  }
  

  private V putInEntry(Entry<V> entry, String key, V value) {
    Entry<V> child = entry.children.get(key.charAt(0));
    if (child == null) {
      // no match, add new child
      entry.addChild(new Entry<>(key, value, RANK.incrementAndGet()));
      size++;
      return null;   
    }
    
    String childKey = child.key;
    
    if (childKey.equals(key)) { // exact match
      if (child.value == null) { // new entry
        child.value = new RankedValue<V>(value, RANK.incrementAndGet());
        size++;
        return null;
      }
      
      // update entry
      V previous = child.value.value;
      child.value.value = value;
      return previous;
    }
    
    int len = Math.min(childKey.length(), key.length());
    int childKeyIdx = 0;
    
    while (childKeyIdx < len) {
      if (childKey.charAt(childKeyIdx) != key.charAt(childKeyIdx)) {
        break;
      }
      childKeyIdx++;
    }

    // partial match - common part needs to be pulled out with two children added
    if (childKeyIdx < len) {
      Entry<V> parent = child.parent;
      parent.removeChild(child);
      
      Entry<V> newChild = new Entry<>(childKey.substring(0, childKeyIdx), (V) null, child.rank);
      parent.addChild(newChild);
      
      child.key = childKey.substring(childKeyIdx);
      newChild.addChild(child);
      newChild.addChild(new Entry<>(key.substring(childKeyIdx), value, RANK.incrementAndGet()));
      
      size++;
      return null;
    }
    
    if (childKeyIdx == childKey.length()) { // current child key matched completely
//      if (child.value != null) {
//        return null; // will always match this child, so more restrictive matches are unreachable
//      }
      return putInEntry(child, key.substring(childKeyIdx), value);
    }
    
    // new key is contained in existing - common part needs to be pulled out with one child added
    Entry<V> parent = child.parent;
    parent.removeChild(child);
    
    Entry<V> newChild = new Entry<>(key, value, RANK.incrementAndGet());
    parent.addChild(newChild);
    
    child.key = childKey.substring(childKeyIdx);
    newChild.addChild(child);
    newChild.rank = child.rank;
    
    
    size++;
    return null;
  }
  
  
  @Override
  public V remove(Object key) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public void clear() {
    RANK = new AtomicInteger(0);
    
    root = new Entry<>(null, (V) null, 0);
    
    size = 0;
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public Set<java.util.Map.Entry<String, V>> entrySet() {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  
  static class Entry<V> implements Map.Entry<String, V> {
    
    String key;
    RankedValue<V> value;
    Entry<V> parent;
    Map<Character, Entry<V>> children;
    int rank;
    
    Entry(String key, V value, int rank) {
      this(key, value == null ? null : new RankedValue<V>(value, rank), rank);
    }
    
    Entry(String key, RankedValue<V> value, int rank) {
      this.key = key;
      this.value = value;
      this.children = new HashMap<>();
      this.rank = rank;
    }
    
    @Override
    public String getKey() {
      if (value == null) {
        return null;
      }
      // TODO
      return null;
    }

    @Override
    public V getValue() {
      return value == null ? null : value.value;
    }

    @Override
    public V setValue(V value) {
      V previous = value;
      this.value.value = value;
      return previous;
    }
    
    public Entry<V> addChild(Entry<V> child) {
      child.parent = this;
      return children.put(child.key.charAt(0), child);
    }
    
    public Entry<V> removeChild(Entry<V> child) {
      return children.remove(child.key.charAt(0));
    }
    
  }
  
}

