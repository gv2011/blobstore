package com.github.gv2011.blobstore.core;

import java.nio.file.Path;

import com.github.gv2011.blobstore.BlobFactory;
import com.github.gv2011.blobstore.Blobstore;
import com.github.gv2011.blobstore.DirectoryService;
import com.github.gv2011.util.json.JsonFactory;
import com.github.gv2011.util.json.JsonUtils;

final class BlobFactoryImp implements BlobFactory{

  private final JsonFactory jf = JsonUtils.jsonFactory();
  private final DirectoryService directoryService = new DirectoryServiceImp(jf);

  @Override
  public Blobstore createBlobstore(Path directory) {
    return new BlobstoreImp(directory);
  }

  @Override
  public DirectoryService directoryService() {
    return directoryService ;
  }

}
