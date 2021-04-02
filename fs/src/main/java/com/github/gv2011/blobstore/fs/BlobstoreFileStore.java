package com.github.gv2011.blobstore.fs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileStoreAttributeView;

final class BlobstoreFileStore extends FileStore {

  @Override
  public String name() {
     return "blobstore";
  }

  @Override
  public String type() {
    return "blobstore";
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public long getTotalSpace() throws IOException {
    return Long.MAX_VALUE;
  }

  @Override
  public long getUsableSpace() throws IOException {
    return Long.MAX_VALUE;
  }

  @Override
  public long getUnallocatedSpace() throws IOException {
    return Long.MAX_VALUE;
  }

  @Override
  public boolean supportsFileAttributeView(Class<? extends FileAttributeView> type) {
    return type.equals(BasicFileAttributeView.class);
  }

  @Override
  public boolean supportsFileAttributeView(String name) {
    return "basic".equals(name);
  }

  @Override
  public <V extends FileStoreAttributeView> V getFileStoreAttributeView(Class<V> type) {
    return null;
  }

  @Override
  public Object getAttribute(String attribute) throws IOException {
    return null;
  }

}
