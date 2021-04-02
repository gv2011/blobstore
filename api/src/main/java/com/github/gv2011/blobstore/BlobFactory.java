package com.github.gv2011.blobstore;

import java.nio.file.Path;

public interface BlobFactory {
  
  static final String SCHEME = "blobstore";
  
  Blobstore createBlobstore(Path directory);
  
  DirectoryService directoryService();

}
