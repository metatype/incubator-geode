/*=========================================================================
 * Copyright (c) 2002-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */

package com.gemstone.gemfire.internal.cache;

import com.gemstone.gemfire.cache.*;
import com.gemstone.gemfire.internal.InternalStatisticsDisabledException;
//import com.gemstone.gemfire.InternalGemFireException;
//import com.gemstone.gemfire.internal.util.Callable;
//import com.gemstone.gemfire.util.concurrent.locks.Lock;

/**
 *
 * @author Eric Zoerner
 *
 */
class CacheStatisticsImpl implements CacheStatistics {
  private final LocalRegion region;
  private final RegionEntry regionEntry;
  
  CacheStatisticsImpl(RegionEntry regionEntry, LocalRegion region) {
    this.region = region;
    // entry stats are all on commited state so ok to ignore tx state
    this.regionEntry = regionEntry;
  }
  
  public long getHitCount() throws StatisticsDisabledException {
    checkEntryDestroyed();
    try {
      return this.regionEntry.getHitCount();
    }
    catch (InternalStatisticsDisabledException e) {
      throw new StatisticsDisabledException(e);
    }
  }

  public float getHitRatio() throws StatisticsDisabledException {
    checkEntryDestroyed();
    // Don't worry about write synchronizing. This is just a stat
    // so its ok if the hit ratio is inaccurate because the hit count
    // and miss count are sampled without a write lock.
    RegionEntry entry = this.regionEntry;
    try {
      long hitCount = entry.getHitCount();
      long total = hitCount + entry.getMissCount();
      return total == 0L ? 0.0f : ((float)hitCount / total);
    }
    catch (InternalStatisticsDisabledException e) {
      throw new StatisticsDisabledException(e);
    }
  }
  
  public long getLastAccessedTime() throws StatisticsDisabledException {
    checkEntryDestroyed();
    try {
      return this.regionEntry.getLastAccessed();
    }
    catch (InternalStatisticsDisabledException e) {
      throw new StatisticsDisabledException(e);
    }
  }
  
  public long getLastModifiedTime() {
    checkEntryDestroyed();
    return this.regionEntry.getLastModified();
  }
  
  public long getMissCount() throws StatisticsDisabledException {
    checkEntryDestroyed();
    try {
      return this.regionEntry.getMissCount();
    }
    catch (InternalStatisticsDisabledException e) {
      throw new StatisticsDisabledException(e);
    }
  }

  public void resetCounts() throws StatisticsDisabledException {
    checkEntryDestroyed();
    try {
      this.regionEntry.resetCounts();
    }
    catch (InternalStatisticsDisabledException e) {
      throw new StatisticsDisabledException(e);
    }
  }    
  
  /* throws CacheClosedException or EntryDestroyedException if this entry is
   * destroyed.
   */
  private void checkEntryDestroyed() {
    this.region.getCancelCriterion().checkCancelInProgress(null);
    if (this.regionEntry.isRemoved()) {
      throw new EntryDestroyedException(this.regionEntry.getKey().toString());
    }
  }
}
