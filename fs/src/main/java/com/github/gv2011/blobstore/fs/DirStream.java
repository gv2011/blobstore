package com.github.gv2011.blobstore.fs;

import static com.github.gv2011.util.CollectionUtils.mapIterator;

import java.nio.file.DirectoryStream;
import java.util.Iterator;

import com.github.gv2011.blobstore.DirEntry;
import com.github.gv2011.util.icol.IList;

final class DirStream implements DirectoryStream<String>{
  
  private final IList<DirEntry> entries;
  
  DirStream(IList<DirEntry> entries) {
    this.entries = entries;
  }

  @Override
  public void close(){}

  @Override
  public Iterator<String> iterator() {
    return mapIterator(entries.iterator(), DirEntry::name);
  }

}
