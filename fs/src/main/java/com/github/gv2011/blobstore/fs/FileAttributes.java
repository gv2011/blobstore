package com.github.gv2011.blobstore.fs;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import com.github.gv2011.blobstore.DirEntry;

final class FileAttributes implements BasicFileAttributes{
  
  private final DirEntry dirEntry;

  FileAttributes(DirEntry dirEntry) {
    this.dirEntry = dirEntry;
  }

  @Override
  public FileTime lastModifiedTime() {
    return FileTime.from(dirEntry.modified().orElse(Instant.EPOCH));
  }

  @Override
  public FileTime lastAccessTime() {
    return lastModifiedTime();
  }

  @Override
  public FileTime creationTime() {
    return lastModifiedTime();
  }

  @Override
  public boolean isRegularFile() {
    return !isDirectory();
  }

  @Override
  public boolean isDirectory() {
    return dirEntry.isDirectory();
  }

  @Override
  public boolean isSymbolicLink() {
    return false;
  }

  @Override
  public boolean isOther() {
    return false;
  }

  @Override
  public long size() {
     return dirEntry.size();
  }

  @Override
  public Object fileKey() {
    return null;
  }

}
